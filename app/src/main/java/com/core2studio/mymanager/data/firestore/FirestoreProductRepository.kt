package com.core2studio.mymanager.data.firestore

import android.util.Log
import com.core2studio.mymanager.data.local.dao.ProductDao
import com.core2studio.mymanager.data.local.entity.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProductRepository(
    private val productDao: ProductDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    private fun userProductsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("products")

    private fun parseProduct(doc: DocumentSnapshot): Product? {
        val name = doc.getString("name") ?: return null
        val imageUrls = when {
            doc.get("imageUrls") is List<*> -> (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            doc.getString("imageUri") != null -> listOf(doc.getString("imageUri")!!)
            else -> emptyList()
        }
        val cloudinaryPublicIds = when {
            doc.get("cloudinaryPublicIds") is List<*> -> (doc.get("cloudinaryPublicIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            else -> emptyList()
        }
        return Product(
            id = doc.id,
            categoryId = doc.getString("categoryId") ?: "",
            name = name,
            description = doc.getString("description") ?: "",
            price = doc.getDouble("price") ?: 0.0,
            imageUrls = imageUrls,
            cloudinaryPublicIds = cloudinaryPublicIds,
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    fun getAllProducts(uid: String): Flow<List<Product>> = callbackFlow {
        val listener = userProductsCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to products", error)
                    close(error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    parseProduct(doc)
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertProduct(uid: String, product: Product): Result<String> = runCatching {
        val docRef = if (product.id.isNotEmpty()) {
            userProductsCollection(uid).document(product.id)
        } else {
            userProductsCollection(uid).document()
        }
        val data = mapOf(
            "categoryId" to product.categoryId,
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "imageUrls" to product.imageUrls,
            "cloudinaryPublicIds" to product.cloudinaryPublicIds,
            "createdAt" to product.createdAt
        )
        docRef.set(data).await()
        val firestoreId = docRef.id
        productDao.insert(product.copy(id = firestoreId))
        firestoreId
    }

    suspend fun updateProduct(uid: String, product: Product): Result<Unit> = runCatching {
        userProductsCollection(uid).document(product.id).update(
            mapOf(
                "categoryId" to product.categoryId,
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "imageUrls" to product.imageUrls,
                "cloudinaryPublicIds" to product.cloudinaryPublicIds
            )
        ).await()
        productDao.update(product)
    }

    suspend fun deleteProduct(uid: String, product: Product): Result<Unit> = runCatching {
        userProductsCollection(uid).document(product.id).delete().await()
        productDao.delete(product)
    }

    suspend fun fetchAll(uid: String): Result<List<Product>> = runCatching {
        val snapshot = userProductsCollection(uid).get().await()
        snapshot.documents.mapNotNull { doc -> parseProduct(doc) }
    }

    suspend fun syncAll(uid: String): Result<Unit> = runCatching {
        val products = fetchAll(uid).getOrThrow()
        productDao.deleteAll()
        products.forEach { productDao.insert(it) }
    }

    companion object {
        private const val TAG = "FirestoreProductRepo"
    }
}
