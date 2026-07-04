package com.core2studio.mymanager.data.auth

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val isSignedIn: Boolean get() = auth.currentUser != null

    val userId: String? get() = auth.currentUser?.uid

    val userEmail: String? get() = auth.currentUser?.email

    val displayName: String? get() = auth.currentUser?.displayName

    val isEmailVerified: Boolean get() = auth.currentUser?.isEmailVerified ?: false

    suspend fun sendEmailVerification(): Result<Unit> = runCatching {
        auth.currentUser?.sendEmailVerification()?.await()
    }.map { }.onFailure { e ->
        Log.e("AuthRepository", "Failed to send verification email", e)
    }

    suspend fun reloadUser(): Result<Unit> = runCatching {
        auth.currentUser?.reload()?.await()
    }.map { }.onFailure { e ->
        Log.e("AuthRepository", "Failed to reload user", e)
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        )?.await()
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun signInWithGoogle(idToken: String): Result<Boolean> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val isNewUser = result.additionalUserInfo?.isNewUser == true
        return Result.success(isNewUser)
    }

    suspend fun linkGoogleCredential(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.currentUser?.linkWithCredential(credential)?.await()
        Unit
    }

    suspend fun updateDisplayName(newName: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        user.updateProfile(profileUpdates).await()
    }.map { }.onFailure { e ->
        Log.e("AuthRepository", "Failed to update display name", e)
    }

    suspend fun reauthenticate(password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        val email = user.email ?: throw IllegalStateException("No email found")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }.map { }.onFailure { e ->
        Log.d("AuthRepository", "Reauthentication failed: ${e.message}")
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No user signed in")
        user.updatePassword(newPassword).await()
    }.map { }.onFailure { e ->
        Log.e("AuthRepository", "Failed to update password", e)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }.map { }.onFailure { e ->
        Log.e("AuthRepository", "Failed to send password reset email", e)
    }

    fun signOut() {
        auth.signOut()
    }
}
