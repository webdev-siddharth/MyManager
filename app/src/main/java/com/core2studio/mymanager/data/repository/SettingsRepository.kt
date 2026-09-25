package com.core2studio.mymanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.awaitClose

class SettingsRepository(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) {

    data class BusinessSettings(
        val gstin: String = "",
        val gstEnabled: Boolean = false,
        val gstPricingMode: String = "INCLUSIVE",
        val gstRate: Int = 18,
        val gstType: String = "CGST_SGST",
        val businessName: String = "",
        val businessEmail: String = "",
        val businessPhone: String = "",
        val businessAddress: String = "",
        val businessLogoUrl: String = "",
        val website: String = ""
    )

    private fun getPrefs(): SharedPreferences {
        val uid = authRepository.userId ?: "default"
        return context.getSharedPreferences("mymanager_settings_$uid", Context.MODE_PRIVATE)
    }

    private fun loadLocalSettings(): BusinessSettings {
        val prefs = getPrefs()
        return BusinessSettings(
            gstin = prefs.getString("business_gstin", "") ?: "",
            gstEnabled = prefs.getBoolean("gst_enabled", false),
            gstPricingMode = prefs.getString("gst_pricing_mode", "INCLUSIVE") ?: "INCLUSIVE",
            gstRate = prefs.getInt("gst_rate", 18),
            gstType = prefs.getString("gst_type", "CGST_SGST") ?: "CGST_SGST",
            businessName = prefs.getString("business_name", "") ?: "",
            businessEmail = prefs.getString("business_email", "") ?: "",
            businessPhone = prefs.getString("business_phone", "") ?: "",
            businessAddress = prefs.getString("business_address", "") ?: "",
            businessLogoUrl = prefs.getString("business_logo_url", "") ?: "",
            website = prefs.getString("business_website", "") ?: ""
        )
    }

    val settingsFlow = callbackFlow<BusinessSettings> {
        val prefs = getPrefs()
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf("business_gstin", "gst_enabled", "gst_pricing_mode", "gst_rate", "gst_type")) {
                trySend(loadLocalSettings())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        trySend(loadLocalSettings())

        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    suspend fun getMergedSettings(): BusinessSettings = withContext(Dispatchers.IO) {
        val localSettings = loadLocalSettings()
        val uid = authRepository.userId
        if (uid == null) return@withContext localSettings

        try {
            val profile = userProfileRepository.getProfile(uid)
            if (profile != null) {
                localSettings.copy(
                    gstin = profile.gstin.ifEmpty { localSettings.gstin },
                    gstEnabled = profile.gstEnabled,
                    gstPricingMode = profile.gstPricingMode.ifEmpty { localSettings.gstPricingMode },
                    gstRate = if (profile.gstRate > 0) profile.gstRate else localSettings.gstRate,
                    gstType = profile.gstType.ifEmpty { localSettings.gstType },
                    businessName = profile.businessName.ifEmpty { localSettings.businessName },
                    businessEmail = profile.businessEmail.ifEmpty { localSettings.businessEmail },
                    businessPhone = profile.businessPhone.ifEmpty { localSettings.businessPhone },
                    businessAddress = profile.businessAddress.ifEmpty { localSettings.businessAddress },
                    businessLogoUrl = profile.businessLogoUrl.ifEmpty { localSettings.businessLogoUrl },
                    website = profile.website.ifEmpty { localSettings.website }
                )
            } else {
                localSettings
            }
        } catch (e: Exception) {
            localSettings
        }
    }

    suspend fun saveGstSettings(
        pricingMode: String,
        rate: Int,
        type: String,
        gstEnabled: Boolean = false
    ) {
        val prefs = getPrefs()
        val uid = authRepository.userId
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putBoolean("gst_enabled", gstEnabled)
                .putString("gst_pricing_mode", pricingMode)
                .putInt("gst_rate", rate)
                .putString("gst_type", type)
                .apply()
        }
        if (uid != null) {
            val currentSettings = getMergedSettings()
            userProfileRepository.saveBusinessInfo(
                uid = uid,
                businessName = currentSettings.businessName,
                businessEmail = currentSettings.businessEmail,
                businessPhone = currentSettings.businessPhone,
                businessAddress = currentSettings.businessAddress,
                businessLogoUrl = currentSettings.businessLogoUrl,
                gstin = currentSettings.gstin,
                website = currentSettings.website,
                gstEnabled = gstEnabled,
                gstPricingMode = pricingMode,
                gstRate = rate,
                gstType = type
            )
        }
    }

    suspend fun saveBusinessInfo(settings: BusinessSettings) {
        val prefs = getPrefs()
        val uid = authRepository.userId
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString("business_name", settings.businessName)
                .putString("business_email", settings.businessEmail)
                .putString("business_phone", settings.businessPhone)
                .putString("business_address", settings.businessAddress)
                .putString("business_logo_url", settings.businessLogoUrl)
                .putString("business_gstin", settings.gstin)
                .putString("business_website", settings.website)
                .putBoolean("gst_enabled", settings.gstEnabled)
                .putString("gst_pricing_mode", settings.gstPricingMode)
                .putInt("gst_rate", settings.gstRate)
                .putString("gst_type", settings.gstType)
                .apply()
        }
        if (uid != null) {
            userProfileRepository.saveBusinessInfo(
                uid = uid,
                businessName = settings.businessName,
                businessEmail = settings.businessEmail,
                businessPhone = settings.businessPhone,
                businessAddress = settings.businessAddress,
                businessLogoUrl = settings.businessLogoUrl,
                gstin = settings.gstin,
                website = settings.website,
                gstEnabled = settings.gstEnabled,
                gstPricingMode = settings.gstPricingMode,
                gstRate = settings.gstRate,
                gstType = settings.gstType
            )
        }
    }

    suspend fun getCurrentSettings(): BusinessSettings = getMergedSettings()
}