package com.core2studio.mymanager.data.firestore

import android.util.Log
import com.core2studio.mymanager.data.local.dao.OrderDao
import com.core2studio.mymanager.data.local.entity.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreOrderRepository(
    private val orderDao: OrderDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    private fun userOrdersCollection(uid: String) =
        firestore.collection("users").document(uid).collection("orders")

    fun getAllOrders(uid: String): Flow<List<Order>> = callbackFlow {
        val listener = userOrdersCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to orders", error)
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    Order(
                        id = doc.id,
                        clientId = doc.getString("clientId") ?: "",
                        productId = doc.getString("productId"),
                        productName = doc.getString("productName") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        paidAmount = doc.getDouble("paidAmount") ?: 0.0,
                        status = doc.getString("status") ?: "PENDING",
                        customFields = doc.getString("customFields") ?: "{}",
                        notes = doc.getString("notes") ?: "",
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertOrder(uid: String, order: Order): Result<String> = runCatching {
        val docRef = if (order.id.isNotEmpty()) {
            userOrdersCollection(uid).document(order.id)
        } else {
            userOrdersCollection(uid).document()
        }
        val data = mapOf(
            "clientId" to order.clientId,
            "productId" to order.productId,
            "productName" to order.productName,
            "amount" to order.amount,
            "paidAmount" to order.paidAmount,
            "status" to order.status,
            "customFields" to order.customFields,
            "notes" to order.notes,
            "date" to order.date,
            "createdAt" to order.createdAt
        )
        docRef.set(data).await()
        val firestoreId = docRef.id
        orderDao.insert(order.copy(id = firestoreId))
        firestoreId
    }

    suspend fun updateOrder(uid: String, order: Order): Result<Unit> = runCatching {
        userOrdersCollection(uid).document(order.id).update(
            mapOf(
                "clientId" to order.clientId,
                "productId" to order.productId,
                "productName" to order.productName,
                "amount" to order.amount,
                "paidAmount" to order.paidAmount,
                "status" to order.status,
                "customFields" to order.customFields,
                "notes" to order.notes,
                "date" to order.date
            )
        ).await()
        orderDao.update(order)
    }

    suspend fun deleteOrder(uid: String, order: Order): Result<Unit> = runCatching {
        userOrdersCollection(uid).document(order.id).delete().await()
        orderDao.delete(order)
    }

    suspend fun fetchAll(uid: String): Result<List<Order>> = runCatching {
        val snapshot = userOrdersCollection(uid).get().await()
        snapshot.documents.mapNotNull { doc ->
            Order(
                id = doc.id,
                clientId = doc.getString("clientId") ?: "",
                productId = doc.getString("productId"),
                productName = doc.getString("productName") ?: "",
                amount = doc.getDouble("amount") ?: 0.0,
                paidAmount = doc.getDouble("paidAmount") ?: 0.0,
                status = doc.getString("status") ?: "PENDING",
                customFields = doc.getString("customFields") ?: "{}",
                notes = doc.getString("notes") ?: "",
                date = doc.getLong("date") ?: System.currentTimeMillis(),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun syncAll(uid: String): Result<Unit> = runCatching {
        val orders = fetchAll(uid).getOrThrow()
        orderDao.deleteAll()
        orders.forEach { orderDao.insert(it) }
    }

    companion object {
        private const val TAG = "FirestoreOrderRepo"
    }
}
