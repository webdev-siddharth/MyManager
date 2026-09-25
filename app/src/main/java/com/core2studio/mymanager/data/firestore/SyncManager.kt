package com.core2studio.mymanager.data.firestore

import android.util.Log
import androidx.room.withTransaction
import com.core2studio.mymanager.data.local.MyManagerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SyncManager(
    private val database: MyManagerDatabase,
    private val firestoreCategoryRepository: FirestoreCategoryRepository,
    private val firestoreClientRepository: FirestoreClientRepository,
    private val firestoreProductRepository: FirestoreProductRepository,
    private val firestoreOrderRepository: FirestoreOrderRepository,
    private val firestoreCartRepository: FirestoreCartRepository
) {
    /** Serialises syncs — login, refresh and manual syncs used to run concurrently. */
    private val syncMutex = Mutex()

    suspend fun syncAllFromFirestore(uid: String): Result<Unit> = syncMutex.withLock {
        // Phase 1: Fetch all Firestore collections in parallel
        val (catResult, cliResult, prodResult, ordResult, cartResult) = coroutineScope {
            val cats = async { firestoreCategoryRepository.fetchAll(uid) }
            val cli = async { firestoreClientRepository.fetchAll(uid) }
            val prods = async { firestoreProductRepository.fetchAll(uid) }
            val ords = async { firestoreOrderRepository.fetchAll(uid) }
            val cart = async { firestoreCartRepository.fetchAll(uid) }
            FiveResults(cats.await(), cli.await(), prods.await(), ords.await(), cart.await())
        }

        // Phase 2: Check all succeeded — an unsuccessful fetch must never touch local data
        val categories = catResult.getOrElse { e ->
            Log.e(TAG, "Sync categories failed", e)
            return Result.failure(e)
        }
        val clients = cliResult.getOrElse { e ->
            Log.e(TAG, "Sync clients failed", e)
            return Result.failure(e)
        }
        val products = prodResult.getOrElse { e ->
            Log.e(TAG, "Sync products failed", e)
            return Result.failure(e)
        }
        val orders = ordResult.getOrElse { e ->
            Log.e(TAG, "Sync orders failed", e)
            return Result.failure(e)
        }
        val cartItems = cartResult.getOrElse { e ->
            Log.e(TAG, "Sync cart failed", e)
            return Result.failure(e)
        }

        // Phase 3: Atomic swap. Everything happens in a single Room transaction, so a
        // failure at any point rolls back to the previous local state instead of
        // leaving tables half-wiped (the old backup/restore path read its snapshot
        // outside the transaction and only logged a failed restore).
        return try {
            database.withTransaction {
                database.categoryDao().deleteAll()
                categories.forEach { database.categoryDao().insert(it) }

                database.clientDao().deleteAll()
                clients.forEach { database.clientDao().insert(it) }

                database.productDao().deleteAll()
                products.forEach { database.productDao().insert(it) }

                database.orderDao().deleteAll()
                orders.forEach { database.orderDao().insert(it) }

                database.cartItemDao().deleteAll()
                cartItems.forEach { database.cartItemDao().insert(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync commit failed; local data left unchanged", e)
            Result.failure(e)
        }
    }

    /**
     * Drops every locally cached row. Called on sign-out: the tables carry no
     * userId column, so leaving them populated would show the previous account's
     * orders/clients/products to whoever signs in next on this device.
     */
    suspend fun clearLocalData(userId: String?) {
        withContext(Dispatchers.IO) {
            try {
                database.deleteAllUserData(userId ?: "")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear local data", e)
            }
        }
    }

    private data class FiveResults<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    companion object {
        private const val TAG = "SyncManager"
    }
}
