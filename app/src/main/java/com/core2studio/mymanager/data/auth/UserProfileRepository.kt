package com.core2studio.mymanager.data.auth

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val businessLogoUrl: String = "",
    val gstin: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class UserProfileRepository {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    suspend fun getProfile(uid: String): com.core2studio.mymanager.data.auth.UserProfile? {
        return try {
            val doc = userDoc(uid).get().await()
            doc.toObject(com.core2studio.mymanager.data.auth.UserProfile::class.java)
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Failed to get profile for uid: $uid", e)
            null
        }
    }

    suspend fun createProfile(uid: String, email: String, displayName: String): Result<Unit> = runCatching {
        val profile = UserProfile(
            uid = uid,
            email = email,
            displayName = displayName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        userDoc(uid).set(profile).await()
    }.map { }.onFailure { e ->
        Log.e("UserProfileRepository", "Failed to create profile for uid: $uid", e)
    }

    suspend fun updateProfile(uid: String, displayName: String, email: String? = null): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "updatedAt" to System.currentTimeMillis()
        )
        if (email != null) {
            updates["email"] = email
        }
        userDoc(uid).update(updates).await()
    }.map { }.onFailure { e ->
        Log.e("UserProfileRepository", "Failed to update profile for uid: $uid", e)
    }

    suspend fun saveBusinessInfo(
        uid: String,
        businessName: String,
        businessEmail: String,
        businessPhone: String,
        businessAddress: String,
        businessLogoUrl: String = "",
        gstin: String = ""
    ): Result<Unit> = runCatching {
        val updates = mapOf(
            "businessName" to businessName,
            "businessEmail" to businessEmail,
            "businessPhone" to businessPhone,
            "businessAddress" to businessAddress,
            "businessLogoUrl" to businessLogoUrl,
            "gstin" to gstin,
            "updatedAt" to System.currentTimeMillis()
        )
        userDoc(uid).set(updates, SetOptions.merge()).await()
    }.map { }.onFailure { e ->
        Log.e("UserProfileRepository", "Failed to save business info for uid: $uid", e)
    }

    suspend fun deleteProfile(uid: String): Result<Unit> = runCatching {
        userDoc(uid).delete().await()
    }.map { }.onFailure { e ->
        Log.e("UserProfileRepository", "Failed to delete profile for uid: $uid", e)
    }
}
