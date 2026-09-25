package com.core2studio.mymanager.data.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicInteger

class FirestoreUserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreUserRepo"

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    private fun userCollection(uid: String, collection: String) = userDoc(uid).collection(collection)

    suspend fun deleteUserAccount(uid: String): Result<List<String>> = runCatching {
        val cloudinaryPublicIds = mutableListOf<String>()

        val subcollections = listOf("products", "clients", "orders", "cart", "categories")

        // A WriteBatch is single-use: commit() invalidates it, so it has to be
        // recreated after every commit. Reusing it (the old code did) throws on the
        // 500th document and aborts the deletion, orphaning everything after it.
        var batch = firestore.batch()
        var deleteCount = 0

        for (collection in subcollections) {
            val snapshot = userCollection(uid, collection).get().await()
            for (doc in snapshot.documents) {
                if (collection == "products") {
                    val ids = doc.get("cloudinaryPublicIds")
                    if (ids is List<*>) {
                        cloudinaryPublicIds.addAll(ids.filterIsInstance<String>())
                    }
                }
                batch.delete(doc.reference)
                deleteCount++
                if (deleteCount % 500 == 0) {
                    batch.commit().await()
                    batch = firestore.batch()
                    deleteCount = 0
                }
            }
        }

        if (deleteCount > 0) {
            batch.commit().await()
            batch = firestore.batch()
        }

        batch.delete(userDoc(uid))
        batch.commit().await()

        Log.d(TAG, "Deleted user account and all subcollections for uid: $uid")
        cloudinaryPublicIds
    }

    suspend fun deleteSubcollection(uid: String, collection: String): Result<Unit> = runCatching {
        val snapshot = userCollection(uid, collection).get().await()
        var batch = firestore.batch()
        var count = 0

        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
            count++
            if (count % 500 == 0) {
                batch.commit().await()
                batch = firestore.batch()
                count = 0
            }
        }

        if (count > 0) {
            batch.commit().await()
        }

        Log.d(TAG, "Deleted subcollection $collection for uid: $uid")
    }
}