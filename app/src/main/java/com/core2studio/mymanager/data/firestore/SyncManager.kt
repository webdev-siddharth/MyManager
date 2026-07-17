package com.core2studio.mymanager.data.firestore

import android.util.Log
import androidx.room.withTransaction
import com.core2studio.mymanager.data.local.MyManagerDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SyncManager(
    private val database: MyManagerDatabase,
    private val firestoreCategoryRepository: FirestoreCategoryRepository,
    private val firestoreClientRepository: FirestoreClientRepository,
    private val firestoreProductRepository: FirestoreProductRepository,
    private val firestoreOrderRepository: FirestoreOrderRepository,
    private val firestoreCartRepository: FirestoreCartRepository
) {
    suspend fun syncAllFromFirestore(uid: String): Result<Unit> {
        // Phase 1: Fetch all Firestore collections in parallel
        val (catResult, cliResult, prodResult, ordResult, cartResult) = coroutineScope {
            val cats = async { firestoreCategoryRepository.fetchAll(uid) }
            val cli = async { firestoreClientRepository.fetchAll(uid) }
            val prods = async { firestoreProductRepository.fetchAll(uid) }
            val ords = async { firestoreOrderRepository.fetchAll(uid) }
            val cart = async { firestoreCartRepository.fetchAll(uid) }
            FiveResults(cats.await(), cli.await(), prods.await(), ords.await(), cart.await())
        }

        // Phase 2: Check all succeeded
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

        // Phase 3: Snapshot current local data, then commit all to Room
        val backupCategories = database.categoryDao().getAllOnce()
        val backupClients = database.clientDao().getAllOnce()
        val backupProducts = database.productDao().getAllOnce()
        val backupOrders = database.orderDao().getAllOrdersOnce()
        val backupCartItems = database.cartItemDao().getAllOnce()

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
            Log.e(TAG, "Transaction commit failed, restoring backup", e)
            try {
                database.withTransaction {
                    backupCategories.forEach { database.categoryDao().insert(it) }
                    backupClients.forEach { database.clientDao().insert(it) }
                    backupProducts.forEach { database.productDao().insert(it) }
                    backupOrders.forEach { database.orderDao().insert(it) }
                    backupCartItems.forEach { database.cartItemDao().insert(it) }
                }
            } catch (restoreEx: Exception) {
                Log.e(TAG, "Backup restore also failed", restoreEx)
            }
            Result.failure(e)
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
