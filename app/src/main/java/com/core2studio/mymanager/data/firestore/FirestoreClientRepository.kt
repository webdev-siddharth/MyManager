package com.core2studio.mymanager.data.firestore

import android.util.Log
import com.core2studio.mymanager.data.local.dao.ClientDao
import com.core2studio.mymanager.data.local.entity.Client
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreClientRepository(
    private val clientDao: ClientDao
) {
    private val firestore = FirebaseFirestore.getInstance()

    private fun userClientsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("clients")

    fun getAllClients(uid: String): Flow<List<Client>> = callbackFlow {
        val listener = userClientsCollection(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to clients", error)
                    close(error)
                    return@addSnapshotListener
                }
                val clients = snapshot?.documents?.mapNotNull { doc ->
                    Client(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        email = doc.getString("email") ?: "",
                        address = doc.getString("address") ?: "",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                trySend(clients)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertClient(uid: String, client: Client): Result<String> = runCatching {
        val docRef = if (client.id.isNotEmpty()) {
            userClientsCollection(uid).document(client.id)
        } else {
            userClientsCollection(uid).document()
        }
        val data = mapOf(
            "name" to client.name,
            "phone" to client.phone,
            "email" to client.email,
            "address" to client.address,
            "createdAt" to client.createdAt
        )
        docRef.set(data).await()
        val firestoreId = docRef.id
        clientDao.insert(client.copy(id = firestoreId))
        firestoreId
    }

    suspend fun updateClient(uid: String, client: Client): Result<Unit> = runCatching {
        userClientsCollection(uid).document(client.id).update(
            mapOf(
                "name" to client.name,
                "phone" to client.phone,
                "email" to client.email,
                "address" to client.address
            )
        ).await()
        clientDao.update(client)
    }

    suspend fun deleteClient(uid: String, client: Client): Result<Unit> = runCatching {
        userClientsCollection(uid).document(client.id).delete().await()
        clientDao.delete(client)
    }

    suspend fun fetchAll(uid: String): Result<List<Client>> = runCatching {
        val snapshot = userClientsCollection(uid).get().await()
        snapshot.documents.mapNotNull { doc ->
            Client(
                id = doc.id,
                name = doc.getString("name") ?: "",
                phone = doc.getString("phone") ?: "",
                email = doc.getString("email") ?: "",
                address = doc.getString("address") ?: "",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun syncAll(uid: String): Result<Unit> = runCatching {
        val clients = fetchAll(uid).getOrThrow()
        clientDao.deleteAll()
        clients.forEach { clientDao.insert(it) }
    }

    companion object {
        private const val TAG = "FirestoreClientRepo"
    }
}
