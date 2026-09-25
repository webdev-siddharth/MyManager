package com.core2studio.mymanager.ui.screens.settings

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.firestore.FirestoreUserRepository
import com.core2studio.mymanager.data.local.MyManagerDatabase
import com.core2studio.mymanager.data.storage.CloudinaryStorage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.text.ifEmpty

data class SettingsUiState(
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val businessLogoUrl: String = "",
    val gstin: String = "",
    val website: String = "",
    val gstEnabled: Boolean = false,
    val gstPricingMode: String = "INCLUSIVE",
    val gstRate: Int = 18,
    val gstType: String = "CGST_SGST",
    val accountEmail: String? = null,
    val message: String? = null,
    val isUploadingLogo: Boolean = false,
    val themeMode: Int = 0,
    val currencyCode: String = com.core2studio.mymanager.data.utils.CurrencyUtils.DEFAULT_CURRENCY
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val firestoreUserRepository: FirestoreUserRepository,
    private val database: MyManagerDatabase,
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
        private const val KEY_GST_ENABLED = "gst_enabled"
        private const val KEY_GST_PRICING_MODE = "gst_pricing_mode"
        private const val KEY_GST_RATE = "gst_rate"
        private const val KEY_GST_TYPE = "gst_type"
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

        // Skip if already loaded for this user (avoids duplicate Firestore read on remount)
        if (currentPrefs != null && _uiState.value.accountEmail == authRepository.userEmail) {
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
                    gstEnabled = userPrefs.getBoolean(KEY_GST_ENABLED, false),
                    gstPricingMode = userPrefs.getString(KEY_GST_PRICING_MODE, "INCLUSIVE") ?: "INCLUSIVE",
                    gstRate = userPrefs.getInt(KEY_GST_RATE, 18),
                    gstType = userPrefs.getString(KEY_GST_TYPE, "CGST_SGST") ?: "CGST_SGST",
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
                        website = profile.website.ifEmpty { baseState.website },
                        gstEnabled = profile.gstEnabled,
                        gstPricingMode = profile.gstPricingMode.ifEmpty { baseState.gstPricingMode },
                        gstRate = if (profile.gstRate > 0) profile.gstRate else baseState.gstRate,
                        gstType = profile.gstType.ifEmpty { baseState.gstType }
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
        website: String = "",
        gstEnabled: Boolean = _uiState.value.gstEnabled,
        gstPricingMode: String = _uiState.value.gstPricingMode,
        gstRate: Int = _uiState.value.gstRate,
        gstType: String = _uiState.value.gstType
    ) {
        _uiState.value = _uiState.value.copy(
            businessName = name,
            businessEmail = email,
            businessPhone = phone,
            businessAddress = address,
            businessLogoUrl = logoUrl,
            gstin = gstin,
            website = website,
            gstEnabled = gstEnabled,
            gstPricingMode = gstPricingMode,
            gstRate = gstRate,
            gstType = gstType
        )

        currentPrefs?.edit()
            ?.putString(KEY_BUSINESS_NAME, name)
            ?.putString(KEY_BUSINESS_EMAIL, email)
            ?.putString(KEY_BUSINESS_PHONE, phone)
            ?.putString(KEY_BUSINESS_ADDRESS, address)
            ?.putString(KEY_BUSINESS_LOGO_URL, logoUrl)
            ?.putString(KEY_BUSINESS_GSTIN, gstin)
            ?.putString(KEY_WEBSITE, website)
            ?.putBoolean(KEY_GST_ENABLED, gstEnabled)
            ?.putString(KEY_GST_PRICING_MODE, gstPricingMode)
            ?.putInt(KEY_GST_RATE, gstRate)
            ?.putString(KEY_GST_TYPE, gstType)
            ?.apply()

        val uid = authRepository.userId
        if (uid != null) {
            viewModelScope.launch {
                userProfileRepository.saveBusinessInfo(uid, name, email, phone, address, logoUrl, gstin, website, gstEnabled, gstPricingMode, gstRate, gstType)
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
                    _uiState.value.website,
                    _uiState.value.gstEnabled,
                    _uiState.value.gstPricingMode,
                    _uiState.value.gstRate,
                    _uiState.value.gstType
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
                    _uiState.value.website,
                    _uiState.value.gstEnabled,
                    _uiState.value.gstPricingMode,
                    _uiState.value.gstRate,
                    _uiState.value.gstType
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
    fun getGstEnabled(): Boolean = _uiState.value.gstEnabled
    fun getGstPricingMode(): String = _uiState.value.gstPricingMode
    fun getGstRate(): Int = _uiState.value.gstRate
    fun getGstType(): String = _uiState.value.gstType

    fun signOut() {
        // Clear Google session; Firebase sign-out is owned by AuthViewModel.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
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

    fun deleteAccount(password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: run {
                onResult(false, "User not signed in")
                return@launch
            }

            _uiState.value = _uiState.value.copy(message = "Deleting account...")

            val errors = mutableListOf<String>()

            try {
                authRepository.reauthenticate(password).onFailure { e ->
                    onResult(false, "Re-authentication failed: ${e.message}")
                    return@launch
                }

                val profile = userProfileRepository.getProfile(uid)
                val logoPublicId = extractPublicIdFromUrl(profile?.businessLogoUrl)

                // 1. Cloud data first: Firestore rules require an authenticated session,
                //    so the auth user may only be removed after its documents are gone.
                //    Aborting here leaves local data untouched, so a retry is safe.
                val firestoreRes = firestoreUserRepository.deleteUserAccount(uid)
                firestoreRes.onFailure { e ->
                    android.util.Log.e("SettingsViewModel", "Failed to delete Firestore data", e)
                    onResult(
                        false,
                        "Could not delete your cloud data: ${e.message}. Nothing was removed locally — please try again."
                    )
                    return@launch
                }
                val productPublicIds = firestoreRes.getOrDefault(emptyList())

                // 2. Cloudinary assets (best effort: report as warnings, never fatal)
                val allCloudinaryIds = buildList {
                    logoPublicId?.let { add(it) }
                    addAll(productPublicIds)
                }
                if (allCloudinaryIds.isNotEmpty()) {
                    cloudinaryStorage.deleteImages(allCloudinaryIds).onFailure { e ->
                        errors.add("Cloudinary images: ${e.message}")
                        android.util.Log.e("SettingsViewModel", "Failed to delete Cloudinary images", e)
                    }
                }

                // 3. Remove the auth account. Only once this succeeds is it correct to
                //    report success or wipe the local database.
                authRepository.deleteUser().onFailure { e ->
                    android.util.Log.e("SettingsViewModel", "Failed to delete auth user", e)
                    onResult(false, "Could not finish deleting your account: ${e.message}. Please try again.")
                    return@launch
                }

                try {
                    withContext(Dispatchers.IO) {
                        database.deleteAllUserData(uid)
                    }
                } catch (e: Exception) {
                    errors.add("Local database: ${e.message}")
                    android.util.Log.e("SettingsViewModel", "Failed to delete local data", e)
                }

                val prefsFile = File(context.filesDir.parent, "shared_prefs/${PREFS_PREFIX}$uid.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                }

                _uiState.value = SettingsUiState(accountEmail = null)
                currentPrefs = null

                val message = if (errors.isEmpty()) {
                    "Account deleted successfully"
                } else {
                    "Account deleted with warnings: ${errors.joinToString(", ")}"
                }
                onResult(true, message)

            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Account deletion failed", e)
                onResult(false, "Failed to delete account: ${e.message}")
            }
        }
    }

    private fun extractPublicIdFromUrl(url: String?): String? {
        return url?.let {
            val pattern = "upload/v\\d+/(.+)\\.".toRegex()
            pattern.find(it)?.groupValues?.get(1)
        }
    }
}
