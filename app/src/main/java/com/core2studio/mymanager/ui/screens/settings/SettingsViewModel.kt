package com.core2studio.mymanager.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.storage.CloudinaryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty

data class SettingsUiState(
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val businessLogoUrl: String = "",
    val gstin: String = "",
    val accountEmail: String? = null,
    val message: String? = null,
    val isUploadingLogo: Boolean = false,
    val themeMode: Int = 0
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val cloudinaryStorage: CloudinaryStorage,
    private val context: Context
) : ViewModel() {

    companion object {
        private const val PREFS_PREFIX = "mymanager_settings_"
        private const val KEY_BUSINESS_NAME = "business_name"
        private const val KEY_BUSINESS_EMAIL = "business_email"
        private const val KEY_BUSINESS_PHONE = "business_phone"
        private const val KEY_BUSINESS_ADDRESS = "business_address"
        private const val KEY_BUSINESS_LOGO_URL = "business_logo_url"
        private const val KEY_BUSINESS_GSTIN = "business_gstin"
    }

    private var currentPrefs: SharedPreferences? = null

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings() {
        val uid = authRepository.userId

        if (uid == null) {
            _uiState.value = SettingsUiState()
            currentPrefs = null
            return
        }

        val userPrefs = context.getSharedPreferences("$PREFS_PREFIX$uid", Context.MODE_PRIVATE)
        currentPrefs = userPrefs

        _uiState.value = _uiState.value.copy(
            businessName = userPrefs.getString(KEY_BUSINESS_NAME, "") ?: "",
            businessEmail = userPrefs.getString(KEY_BUSINESS_EMAIL, "") ?: "",
            businessPhone = userPrefs.getString(KEY_BUSINESS_PHONE, "") ?: "",
            businessAddress = userPrefs.getString(KEY_BUSINESS_ADDRESS, "") ?: "",
            businessLogoUrl = userPrefs.getString(KEY_BUSINESS_LOGO_URL, "") ?: "",
            gstin = userPrefs.getString(KEY_BUSINESS_GSTIN, "") ?: "",
            accountEmail = authRepository.userEmail,
            themeMode = context.getSharedPreferences("mymanager_theme", Context.MODE_PRIVATE)
                .getInt("theme_mode", 0)
        )

        viewModelScope.launch {
            val profile = userProfileRepository.getProfile(uid)
            if (profile != null) {
                _uiState.value = _uiState.value.copy(
                    businessName = profile.businessName.ifEmpty { _uiState.value.businessName },
                    businessEmail = profile.businessEmail.ifEmpty { _uiState.value.businessEmail },
                    businessPhone = profile.businessPhone.ifEmpty { _uiState.value.businessPhone },
                    businessAddress = profile.businessAddress.ifEmpty { _uiState.value.businessAddress },
                    businessLogoUrl = profile.businessLogoUrl.ifEmpty { _uiState.value.businessLogoUrl },
                    gstin = profile.gstin.ifEmpty { _uiState.value.gstin }
                )
            }
        }
    }

    fun saveBusinessInfo(
        name: String,
        email: String,
        phone: String,
        address: String,
        logoUrl: String = _uiState.value.businessLogoUrl,
        gstin: String = ""
    ) {
        _uiState.value = _uiState.value.copy(
            businessName = name,
            businessEmail = email,
            businessPhone = phone,
            businessAddress = address,
            businessLogoUrl = logoUrl,
            gstin = gstin
        )

        currentPrefs?.edit()
            ?.putString(KEY_BUSINESS_NAME, name)
            ?.putString(KEY_BUSINESS_EMAIL, email)
            ?.putString(KEY_BUSINESS_PHONE, phone)
            ?.putString(KEY_BUSINESS_ADDRESS, address)
            ?.putString(KEY_BUSINESS_LOGO_URL, logoUrl)
            ?.putString(KEY_BUSINESS_GSTIN, gstin)
            ?.apply()

        val uid = authRepository.userId
        if (uid != null) {
            viewModelScope.launch {
                userProfileRepository.saveBusinessInfo(uid, name, email, phone, address, logoUrl, gstin)
            }
        }
    }

    fun uploadLogo(uri: Uri) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            _uiState.value = _uiState.value.copy(isUploadingLogo = true)
            val result = cloudinaryStorage.uploadImage(uri, context, uid)
            if (result != null) {
                val url = result.first
                _uiState.value = _uiState.value.copy(
                    businessLogoUrl = url,
                    isUploadingLogo = false
                )
                // Persist the new logo URL
                currentPrefs?.edit()
                    ?.putString(KEY_BUSINESS_LOGO_URL, url)
                    ?.apply()
                userProfileRepository.saveBusinessInfo(
                    uid,
                    _uiState.value.businessName,
                    _uiState.value.businessEmail,
                    _uiState.value.businessPhone,
                    _uiState.value.businessAddress,
                    url,
                    _uiState.value.gstin
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isUploadingLogo = false,
                    message = "Failed to upload logo"
                )
            }
        }
    }

    fun clearLogo() {
        _uiState.value = _uiState.value.copy(businessLogoUrl = "")
        currentPrefs?.edit()
            ?.putString(KEY_BUSINESS_LOGO_URL, "")
            ?.apply()
        val uid = authRepository.userId
        if (uid != null) {
            viewModelScope.launch {
                userProfileRepository.saveBusinessInfo(
                    uid,
                    _uiState.value.businessName,
                    _uiState.value.businessEmail,
                    _uiState.value.businessPhone,
                    _uiState.value.businessAddress,
                    "",
                    _uiState.value.gstin
                )
            }
        }
    }

    fun getBusinessName(): String = _uiState.value.businessName
    fun getBusinessEmail(): String = _uiState.value.businessEmail
    fun getBusinessPhone(): String = _uiState.value.businessPhone
    fun getBusinessAddress(): String = _uiState.value.businessAddress
    fun getBusinessLogoUrl(): String = _uiState.value.businessLogoUrl
    fun getGstin(): String = _uiState.value.gstin

    fun signOut() {
        _uiState.value = SettingsUiState(accountEmail = null)
        currentPrefs = null
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun saveThemeMode(mode: Int) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        context.getSharedPreferences("mymanager_theme", Context.MODE_PRIVATE)
            .edit()
            .putInt("theme_mode", mode)
            .apply()
    }
}
