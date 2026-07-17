package com.core2studio.mymanager.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.storage.CloudinaryStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.ifEmpty

data class SettingsUiState(
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val businessLogoUrl: String = "",
    val gstin: String = "",
    val website: String = "",
    val accountEmail: String? = null,
    val message: String? = null,
    val isUploadingLogo: Boolean = false,
    val themeMode: Int = 0,
    val currencyCode: String = com.core2studio.mymanager.data.utils.CurrencyUtils.DEFAULT_CURRENCY
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
        private const val KEY_WEBSITE = "business_website"
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

        viewModelScope.launch {
            val prefsState = withContext(Dispatchers.IO) {
                val userPrefs = context.getSharedPreferences("$PREFS_PREFIX$uid", Context.MODE_PRIVATE)
                currentPrefs = userPrefs

                val baseState = SettingsUiState(
                    businessName = userPrefs.getString(KEY_BUSINESS_NAME, "") ?: "",
                    businessEmail = userPrefs.getString(KEY_BUSINESS_EMAIL, "") ?: "",
                    businessPhone = userPrefs.getString(KEY_BUSINESS_PHONE, "") ?: "",
                    businessAddress = userPrefs.getString(KEY_BUSINESS_ADDRESS, "") ?: "",
                    businessLogoUrl = userPrefs.getString(KEY_BUSINESS_LOGO_URL, "") ?: "",
                    gstin = userPrefs.getString(KEY_BUSINESS_GSTIN, "") ?: "",
                    website = userPrefs.getString(KEY_WEBSITE, "") ?: "",
                    accountEmail = authRepository.userEmail,
                    themeMode = context.getSharedPreferences("mymanager_theme", Context.MODE_PRIVATE)
                        .getInt("theme_mode", 0),
                    currencyCode = com.core2studio.mymanager.data.utils.CurrencyUtils.loadCurrencyCode(context)
                )

                val profile = userProfileRepository.getProfile(uid)
                if (profile != null) {
                    baseState.copy(
                        businessName = profile.businessName.ifEmpty { baseState.businessName },
                        businessEmail = profile.businessEmail.ifEmpty { baseState.businessEmail },
                        businessPhone = profile.businessPhone.ifEmpty { baseState.businessPhone },
                        businessAddress = profile.businessAddress.ifEmpty { baseState.businessAddress },
                        businessLogoUrl = profile.businessLogoUrl.ifEmpty { baseState.businessLogoUrl },
                        gstin = profile.gstin.ifEmpty { baseState.gstin },
                        website = profile.website.ifEmpty { baseState.website }
                    )
                } else {
                    baseState
                }
            }
            _uiState.value = prefsState
        }
    }

    fun saveBusinessInfo(
        name: String,
        email: String,
        phone: String,
        address: String,
        logoUrl: String = _uiState.value.businessLogoUrl,
        gstin: String = "",
        website: String = ""
    ) {
        _uiState.value = _uiState.value.copy(
            businessName = name,
            businessEmail = email,
            businessPhone = phone,
            businessAddress = address,
            businessLogoUrl = logoUrl,
            gstin = gstin,
            website = website
        )

        currentPrefs?.edit()
            ?.putString(KEY_BUSINESS_NAME, name)
            ?.putString(KEY_BUSINESS_EMAIL, email)
            ?.putString(KEY_BUSINESS_PHONE, phone)
            ?.putString(KEY_BUSINESS_ADDRESS, address)
            ?.putString(KEY_BUSINESS_LOGO_URL, logoUrl)
            ?.putString(KEY_BUSINESS_GSTIN, gstin)
            ?.putString(KEY_WEBSITE, website)
            ?.apply()

        val uid = authRepository.userId
        if (uid != null) {
            viewModelScope.launch {
                userProfileRepository.saveBusinessInfo(uid, name, email, phone, address, logoUrl, gstin, website)
                    .onFailure { e ->
                        android.util.Log.e("SettingsViewModel", "Failed to save business info", e)
                    }
            }
        }
    }

    fun uploadLogo(uri: Uri) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            _uiState.value = _uiState.value.copy(isUploadingLogo = true)
            val result = cloudinaryStorage.uploadImage(uri, context, uid)
            result.onSuccess { (url, _) ->
                _uiState.value = _uiState.value.copy(
                    businessLogoUrl = url,
                    isUploadingLogo = false
                )
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
                    _uiState.value.gstin,
                    _uiState.value.website
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isUploadingLogo = false,
                    message = "Failed to upload logo: ${e.message}"
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
                    _uiState.value.gstin,
                    _uiState.value.website
                ).onFailure { e ->
                    android.util.Log.e("SettingsViewModel", "Failed to save business info after logo clear", e)
                }
            }
        }
    }

    fun getBusinessName(): String = _uiState.value.businessName
    fun getBusinessEmail(): String = _uiState.value.businessEmail
    fun getBusinessPhone(): String = _uiState.value.businessPhone
    fun getBusinessAddress(): String = _uiState.value.businessAddress
    fun getBusinessLogoUrl(): String = _uiState.value.businessLogoUrl
    fun getGstin(): String = _uiState.value.gstin
    fun getWebsite(): String = _uiState.value.website

    fun signOut() {
        _uiState.value = SettingsUiState(accountEmail = null)
        currentPrefs = null
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun saveThemeMode(mode: Int) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        val app = context.applicationContext as com.core2studio.mymanager.MyManagerApplication
        app.themeMode.intValue = mode
        context.getSharedPreferences("mymanager_theme", Context.MODE_PRIVATE)
            .edit()
            .putInt("theme_mode", mode)
            .apply()
    }

    fun saveCurrency(code: String) {
        _uiState.value = _uiState.value.copy(currencyCode = code)
        val app = context.applicationContext as com.core2studio.mymanager.MyManagerApplication
        app.currencyCode.value = code
        com.core2studio.mymanager.data.utils.CurrencyUtils.saveCurrencyCode(context, code)
    }
}
