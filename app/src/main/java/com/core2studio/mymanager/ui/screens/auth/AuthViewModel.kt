package com.core2studio.mymanager.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.firestore.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isGoogleSignInTriggered: Boolean = false,
    val googleSignInFromSignup: Boolean = false,
    val pendingGoogleIdToken: String? = null,
    val showVerificationScreen: Boolean = false,
    val showBusinessInfoSetup: Boolean = false,
    val isEmailSignupPending: Boolean = false,
    val pendingBusinessName: String = "",
    val pendingBusinessEmail: String = "",
    val pendingBusinessPhone: String = "",
    val pendingBusinessAddress: String = "",
    val pendingGstin: String = "",
    val pendingWebsite: String = "",
    val verificationMessage: String? = null,
    val passwordResetMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(com.core2studio.mymanager.ui.screens.auth.AuthUiState())
    val uiState: StateFlow<com.core2studio.mymanager.ui.screens.auth.AuthUiState> = _uiState.asStateFlow()

    init {
        if (authRepository.isSignedIn) {
            _uiState.value = _uiState.value.copy(
                displayName = authRepository.displayName ?: "",
                email = authRepository.userEmail ?: ""
            )
            viewModelScope.launch {
                authRepository.reloadUser()
                if (authRepository.isEmailVerified) {
                    _uiState.value = _uiState.value.copy(
                        isSignedIn = true,
                        displayName = authRepository.displayName ?: "",
                        email = authRepository.userEmail ?: ""
                    )
                    syncDataFromFirestore()
                } else {
                    _uiState.value = _uiState.value.copy(
                        showVerificationScreen = true,
                        email = authRepository.userEmail ?: ""
                    )
                }
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun setDisplayName(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name, error = null)
    }

    fun updateConfirmPassword(confirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirm, error = null)
    }

    fun updatePendingBusinessName(name: String) {
        _uiState.value = _uiState.value.copy(pendingBusinessName = name, error = null)
    }

    fun updatePendingBusinessEmail(email: String) {
        _uiState.value = _uiState.value.copy(pendingBusinessEmail = email, error = null)
    }

    fun updatePendingBusinessPhone(phone: String) {
        _uiState.value = _uiState.value.copy(pendingBusinessPhone = phone, error = null)
    }

    fun updatePendingBusinessAddress(address: String) {
        _uiState.value = _uiState.value.copy(pendingBusinessAddress = address, error = null)
    }

    fun updatePendingGstin(gstin: String) {
        _uiState.value = _uiState.value.copy(pendingGstin = gstin, error = null)
    }

    fun updatePendingWebsite(website: String) {
        _uiState.value = _uiState.value.copy(pendingWebsite = website, error = null)
    }

    fun signUp() {
        val state = _uiState.value
        if (state.displayName.isBlank()) {
            _uiState.value = state.copy(error = "Name is required")
            return
        }
        if (state.email.isBlank()) {
            _uiState.value = state.copy(error = "Email is required")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "Password is required")
            return
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.signUp(state.email, state.password, state.displayName)
            result.onSuccess {
                val uid = authRepository.userId
                if (uid != null) {
                    val profileResult = userProfileRepository.createProfile(uid, state.email, state.displayName)
                    profileResult.onFailure { e ->
                        android.util.Log.e("AuthViewModel", "Failed to create user profile", e)
                    }
                }
                val pendingToken = _uiState.value.pendingGoogleIdToken
                if (pendingToken != null) {
                    authRepository.linkGoogleCredential(pendingToken).onFailure { e ->
                        android.util.Log.e("AuthViewModel", "Failed to link Google credential", e)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSignedIn = true, pendingGoogleIdToken = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBusinessInfoSetup = true,
                        isEmailSignupPending = true,
                        pendingBusinessEmail = state.email
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Sign up failed")
            }
        }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.value = state.copy(error = "Email is required")
            return
        }
        if (state.password.isBlank()) {
            _uiState.value = state.copy(error = "Password is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.signIn(_uiState.value.email, _uiState.value.password)
            result.onSuccess {
                val pendingToken = _uiState.value.pendingGoogleIdToken
                if (pendingToken != null) {
                    val linkResult = authRepository.linkGoogleCredential(pendingToken)
                    linkResult.onSuccess {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSignedIn = true, pendingGoogleIdToken = null)
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(isLoading = false, isSignedIn = true, pendingGoogleIdToken = null)
                    }
                } else {
                    authRepository.reloadUser()
                    if (authRepository.isEmailVerified) {
                        syncDataFromFirestore()
                        _uiState.value = _uiState.value.copy(isLoading = false, isSignedIn = true)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            showVerificationScreen = true,
                            email = _uiState.value.email
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Sign in failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        val fromSignup = _uiState.value.googleSignInFromSignup
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.signInWithGoogle(idToken)
            result.onSuccess { isNewUser ->
                if (isNewUser && fromSignup) {
                    // Signup + Google + new user -> create profile, show business info setup
                    val uid = authRepository.userId
                    val userEmail = authRepository.userEmail
                    val name = authRepository.displayName
                    if (uid != null) {
                        userProfileRepository.createProfile(uid, userEmail ?: "", name ?: "").onFailure { e ->
                            android.util.Log.e("AuthViewModel", "Failed to create profile (Google signup)", e)
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBusinessInfoSetup = true,
                        isEmailSignupPending = false,
                        pendingBusinessName = name ?: "",
                        pendingBusinessEmail = userEmail ?: ""
                    )
                } else if (isNewUser && !fromSignup) {
                    // Login + Google + new user -> auto-create account, show business info setup
                    val uid = authRepository.userId
                    val userEmail = authRepository.userEmail
                    val name = authRepository.displayName
                    if (uid != null) {
                        userProfileRepository.createProfile(uid, userEmail ?: "", name ?: "").onFailure { e ->
                            android.util.Log.e("AuthViewModel", "Failed to create profile (Google login)", e)
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBusinessInfoSetup = true,
                        isEmailSignupPending = false,
                        pendingBusinessName = name ?: "",
                        pendingBusinessEmail = userEmail ?: ""
                    )
                } else {
                    // Existing user → sign in (both login and signup contexts)
                    val uid = authRepository.userId
                    val userEmail = authRepository.userEmail
                    val name = authRepository.displayName
                    if (uid != null) {
                        val existing = userProfileRepository.getProfile(uid)
                        if (existing == null) {
                            userProfileRepository.createProfile(uid, userEmail ?: "", name ?: "")
                        }
                    }
                    syncDataFromFirestore()
                    _uiState.value = _uiState.value.copy(isLoading = false, isSignedIn = true)
                }
            }.onFailure { e ->
                val message = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection and try again."
                    e.message?.contains("canceled", ignoreCase = true) == true ->
                        "Google sign-in was cancelled. Please try again."
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                        "An account with this email already exists. Please try signing in instead."
                    e.message?.contains("account-exists-with-different-credential", ignoreCase = true) == true ->
                        "An account already exists with this email using a different sign-in method."
                    else ->
                        e.message ?: "Google sign-in failed. Please try again."
                }
                _uiState.value = _uiState.value.copy(isLoading = false, error = message)
            }
        }
    }

    fun triggerGoogleSignIn(fromSignup: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            isGoogleSignInTriggered = true,
            googleSignInFromSignup = fromSignup
        )
    }

    fun onGoogleSignInHandled() {
        _uiState.value = _uiState.value.copy(isGoogleSignInTriggered = false, googleSignInFromSignup = false)
    }

    fun setGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = message,
            isGoogleSignInTriggered = false,
            googleSignInFromSignup = false
        )
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = com.core2studio.mymanager.ui.screens.auth.AuthUiState()
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val result = authRepository.sendEmailVerification()
            result.onSuccess {
                _uiState.value = _uiState.value.copy(verificationMessage = "Verification email sent!")
            }.onFailure {
                _uiState.value = _uiState.value.copy(verificationMessage = "Failed to send email. Please try again.")
            }
        }
    }

    fun checkEmailVerified() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.reloadUser()
            if (authRepository.isEmailVerified) {
                syncDataFromFirestore()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSignedIn = true,
                    showVerificationScreen = false,
                    verificationMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationMessage = "Email not yet verified. Please check your inbox."
                )
            }
        }
    }

    fun clearVerificationMessage() {
        _uiState.value = _uiState.value.copy(verificationMessage = null)
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, passwordResetMessage = null)
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    passwordResetMessage = "If an account exists with this email, a reset link has been sent."
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    passwordResetMessage = "If an account exists with this email, a reset link has been sent."
                )
            }
        }
    }

    fun clearPasswordResetMessage() {
        _uiState.value = _uiState.value.copy(passwordResetMessage = null)
    }

    fun saveBusinessInfoAndContinue() {
        val state = _uiState.value
        if (state.pendingBusinessName.isBlank()) {
            _uiState.value = state.copy(error = "Business name is required")
            return
        }
        if (state.pendingBusinessEmail.isBlank()) {
            _uiState.value = state.copy(error = "Business email is required")
            return
        }
        if (state.pendingBusinessPhone.isBlank()) {
            _uiState.value = state.copy(error = "Business phone is required")
            return
        }
        if (state.pendingBusinessAddress.isBlank()) {
            _uiState.value = state.copy(error = "Business address is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val uid = authRepository.userId
            if (uid == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not found")
                return@launch
            }
            val result = userProfileRepository.saveBusinessInfo(
                uid = uid,
                businessName = state.pendingBusinessName,
                businessEmail = state.pendingBusinessEmail,
                businessPhone = state.pendingBusinessPhone,
                businessAddress = state.pendingBusinessAddress,
                businessLogoUrl = "",
                gstin = state.pendingGstin,
                website = state.pendingWebsite
            )
            result.onSuccess {
                if (state.isEmailSignupPending) {
                    authRepository.sendEmailVerification().onFailure { e ->
                        android.util.Log.e("AuthViewModel", "Failed to send verification email", e)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBusinessInfoSetup = false,
                        showVerificationScreen = true,
                        email = authRepository.userEmail ?: ""
                    )
                } else {
                    syncDataFromFirestore()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showBusinessInfoSetup = false,
                        isSignedIn = true
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save business info"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getDisplayName(): String = authRepository.displayName ?: ""

    fun getEmail(): String = authRepository.userEmail ?: ""

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.updateDisplayName(newName)
            result.onSuccess {
                authRepository.reloadUser().onFailure { e ->
                    android.util.Log.e("AuthViewModel", "Failed to reload user after name update", e)
                }
                val uid = authRepository.userId
                if (uid != null) {
                    userProfileRepository.updateProfile(uid, newName).onFailure { e ->
                        android.util.Log.e("AuthViewModel", "Failed to update profile name in Firestore", e)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    displayName = authRepository.displayName ?: newName
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update name"
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val reauthResult = authRepository.reauthenticate(currentPassword)
            reauthResult.onSuccess {
                val updateResult = authRepository.updatePassword(newPassword)
                updateResult.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update password"
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Current password is incorrect"
                )
            }
        }
    }

    private fun syncDataFromFirestore() {
        val uid = authRepository.userId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                syncManager.syncAllFromFirestore(uid)
            } catch (_: Exception) {
                // Sync failure is non-fatal; local data remains available
            }
        }
    }
}
