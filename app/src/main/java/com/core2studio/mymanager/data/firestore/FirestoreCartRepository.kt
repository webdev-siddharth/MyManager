package com.core2studio.mymanager.data.firestore

import android.util.Log
import com.core2studio.mymanager.data.local.dao.CartItemDao
import com.core2studio.mymanager.data.local.entity.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreCartRepository(
    private val cartItemDao: CartItemDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    private fun userCartCollection(uid: String) =
        firestore.collection("users").document(uid).collection("cart")

    suspend fun insertCartItem(uid: String, cartItem: CartItem): Result<String> = runCatching {
        val docRef = if (cartItem.id.isNotEmpty()) {
            userCartCollection(uid).document(cartItem.id)
        } else {
            userCartCollection(uid).document()
        }
        val data = mapOf(
            "productId" to cartItem.productId,
            "productName" to cartItem.productName,
            "productImage" to cartItem.productImage,
            "price" to cartItem.price,
            "quantity" to cartItem.quantity,
            "addedAt" to cartItem.addedAt
        )
        docRef.set(data).await()
        val firestoreId = docRef.id
        cartItemDao.insert(cartItem.copy(id = firestoreId))
        firestoreId
    }

    suspend fun updateCartItem(uid: String, cartItem: CartItem): Result<Unit> = runCatching {
        userCartCollection(uid).document(cartItem.id).update(
            mapOf(
                "quantity" to cartItem.quantity,
                "price" to cartItem.price
            )
        ).await()
        cartItemDao.update(cartItem)
    }

    suspend fun deleteCartItem(uid: String, cartItem: CartItem): Result<Unit> = runCatching {
        userCartCollection(uid).document(cartItem.id).delete().await()
        cartItemDao.delete(cartItem)
    }

    suspend fun clearCart(uid: String): Result<Unit> = runCatching {
        val snapshot = userCartCollection(uid).get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
        cartItemDao.deleteAll()
    }

    suspend fun fetchAll(uid: String): Result<List<CartItem>> = runCatching {
        val snapshot = userCartCollection(uid).get().await()
        snapshot.documents.mapNotNull { doc ->
            CartItem(
                id = doc.id,
                productId = doc.getString("productId") ?: "",
                productName = doc.getString("productName") ?: "",
                productImage = doc.getString("productImage") ?: "",
                price = doc.getDouble("price") ?: 0.0,
                quantity = (doc.getLong("quantity") ?: 1).toInt(),
                addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun syncAll(uid: String): Result<Unit> = runCatching {
        val cartItems = fetchAll(uid).getOrThrow()
        cartItemDao.deleteAll()
        cartItems.forEach { cartItemDao.insert(it) }
    }

    companion object {
        private const val TAG = "FirestoreCartRepo"
    }
}
