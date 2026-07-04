package com.core2studio.mymanager.data.firestore

import android.util.Log
import com.core2studio.mymanager.data.local.dao.CategoryDao
import com.core2studio.mymanager.data.local.dao.ProductDao
import com.core2studio.mymanager.data.local.entity.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreCategoryRepository(
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    private fun userCategoriesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("categories")

    fun getAllCategories(uid: String): Flow<List<Category>> = callbackFlow {
        val listener = userCategoriesCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to categories", error)
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    Category(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertCategory(uid: String, category: Category): Result<String> = runCatching {
        val docRef = if (category.id.isNotEmpty()) {
            userCategoriesCollection(uid).document(category.id)
        } else {
            userCategoriesCollection(uid).document()
        }
        val data = mapOf(
            "name" to category.name,
            "description" to category.description,
            "createdAt" to category.createdAt
        )
        docRef.set(data).await()
        val firestoreId = docRef.id
        categoryDao.insert(category.copy(id = firestoreId))
        firestoreId
    }

    suspend fun updateCategory(uid: String, category: Category): Result<Unit> = runCatching {
        userCategoriesCollection(uid).document(category.id).update(
            mapOf(
                "name" to category.name,
                "description" to category.description
            )
        ).await()
        categoryDao.update(category)
    }

    suspend fun deleteCategory(uid: String, category: Category): Result<Unit> = runCatching {
        val productsSnapshot = firestore.collection("users").document(uid)
            .collection("products")
            .whereEqualTo("categoryId", category.id)
            .get().await()

        val batch = firestore.batch()
        for (doc in productsSnapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()

        val localProducts = productDao.getProductsByCategoryOnce(category.id)
        localProducts.forEach { productDao.delete(it) }

        userCategoriesCollection(uid).document(category.id).delete().await()
        categoryDao.delete(category)
    }

    suspend fun fetchAll(uid: String): Result<List<Category>> = runCatching {
        val snapshot = userCategoriesCollection(uid).get().await()
        snapshot.documents.mapNotNull { doc ->
            Category(
                id = doc.id,
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun syncAll(uid: String): Result<Unit> = runCatching {
        val categories = fetchAll(uid).getOrThrow()
        categoryDao.deleteAll()
        categories.forEach { categoryDao.insert(it) }
    }

    companion object {
        private const val TAG = "FirestoreCategoryRepo"
    }
}
