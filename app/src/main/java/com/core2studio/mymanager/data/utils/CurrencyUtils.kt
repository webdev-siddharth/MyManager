package com.core2studio.mymanager.data.utils

import android.content.Context
import java.text.NumberFormat
import java.util.Locale

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val displayName: String,
    val locale: Locale
)

object CurrencyUtils {

    val currencies = listOf(
        CurrencyInfo("AED", "AED", "UAE Dirham", Locale.forLanguageTag("ar-AE")),
        CurrencyInfo("AUD", "A$", "Australian Dollar", Locale.forLanguageTag("en-AU")),
        CurrencyInfo("BDT", "\u09F3", "Bangladeshi Taka", Locale.forLanguageTag("bn-BD")),
        CurrencyInfo("BRL", "R$", "Brazilian Real", Locale.forLanguageTag("pt-BR")),
        CurrencyInfo("CAD", "C$", "Canadian Dollar", Locale.forLanguageTag("en-CA")),
        CurrencyInfo("CHF", "CHF", "Swiss Franc", Locale.forLanguageTag("de-CH")),
        CurrencyInfo("CNY", "\u00A5", "Chinese Yuan", Locale.forLanguageTag("zh-CN")),
        CurrencyInfo("EGP", "EGP", "Egyptian Pound", Locale.forLanguageTag("ar-EG")),
        CurrencyInfo("EUR", "\u20AC", "Euro", Locale.forLanguageTag("en-DE")),
        CurrencyInfo("GBP", "\u00A3", "British Pound", Locale.forLanguageTag("en-GB")),
        CurrencyInfo("GHS", "GH\u20B5", "Ghanaian Cedi", Locale.forLanguageTag("en-GH")),
        CurrencyInfo("IDR", "Rp", "Indonesian Rupiah", Locale.forLanguageTag("id-ID")),
        CurrencyInfo("INR", "\u20B9", "Indian Rupee", Locale.forLanguageTag("en-IN")),
        CurrencyInfo("JPY", "\u00A5", "Japanese Yen", Locale.forLanguageTag("ja-JP")),
        CurrencyInfo("KES", "KSh", "Kenyan Shilling", Locale.forLanguageTag("en-KE")),
        CurrencyInfo("KRW", "\u20A9", "South Korean Won", Locale.forLanguageTag("ko-KR")),
        CurrencyInfo("MXN", "MX$", "Mexican Peso", Locale.forLanguageTag("es-MX")),
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit", Locale.forLanguageTag("ms-MY")),
        CurrencyInfo("NGN", "\u20A6", "Nigerian Naira", Locale.forLanguageTag("en-NG")),
        CurrencyInfo("NPR", "NPR", "Nepalese Rupee", Locale.forLanguageTag("ne-NP")),
        CurrencyInfo("PHP", "\u20B1", "Philippine Peso", Locale.forLanguageTag("en-PH")),
        CurrencyInfo("PKR", "PKR", "Pakistani Rupee", Locale.forLanguageTag("en-PK")),
        CurrencyInfo("RUB", "\u20BD", "Russian Ruble", Locale.forLanguageTag("ru-RU")),
        CurrencyInfo("SAR", "SAR", "Saudi Riyal", Locale.forLanguageTag("ar-SA")),
        CurrencyInfo("SGD", "S$", "Singapore Dollar", Locale.forLanguageTag("en-SG")),
        CurrencyInfo("THB", "\u0E3F", "Thai Baht", Locale.forLanguageTag("th-TH")),
        CurrencyInfo("TRY", "\u20BA", "Turkish Lira", Locale.forLanguageTag("tr-TR")),
        CurrencyInfo("USD", "$", "US Dollar", Locale.forLanguageTag("en-US")),
        CurrencyInfo("VND", "\u20AB", "Vietnamese Dong", Locale.forLanguageTag("vi-VN")),
        CurrencyInfo("ZAR", "R", "South African Rand", Locale.forLanguageTag("en-ZA"))
    )

    private const val PREFS_NAME = "mymanager_currency"
    private const val KEY_CURRENCY_CODE = "currency_code"
    const val DEFAULT_CURRENCY = "INR"

    /**
     * NumberFormat instances are expensive to build and are requested for every
     * visible list row on every recomposition, so they are cached per currency code.
     * NumberFormat itself is not thread-safe, hence the synchronization on format().
     */
    private val formatterCache = java.util.concurrent.ConcurrentHashMap<String, NumberFormat>()

    fun getCurrencyByCode(code: String): CurrencyInfo {
        return currencies.find { it.code == code } ?: currencies.first()
    }

    fun getDisplayLabel(currency: CurrencyInfo): String {
        return "${currency.displayName} - ${currency.code} (${currency.symbol})"
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val currency = getCurrencyByCode(currencyCode)
        val formatter = formatterCache.getOrPut(currency.code) {
            NumberFormat.getCurrencyInstance(currency.locale)
        }
        synchronized(formatter) {
            return formatter.format(amount)
        }
    }

    fun getCurrencySymbol(currencyCode: String): String {
        return getCurrencyByCode(currencyCode).symbol
    }

    fun loadCurrencyCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENCY_CODE, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    fun saveCurrencyCode(context: Context, code: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CURRENCY_CODE, code)
            .apply()
    }
}
