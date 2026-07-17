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
        CurrencyInfo("AED", "AED", "UAE Dirham", Locale("ar", "AE")),
        CurrencyInfo("AUD", "A$", "Australian Dollar", Locale("en", "AU")),
        CurrencyInfo("BDT", "\u09F3", "Bangladeshi Taka", Locale("bn", "BD")),
        CurrencyInfo("BRL", "R$", "Brazilian Real", Locale("pt", "BR")),
        CurrencyInfo("CAD", "C$", "Canadian Dollar", Locale("en", "CA")),
        CurrencyInfo("CHF", "CHF", "Swiss Franc", Locale("de", "CH")),
        CurrencyInfo("CNY", "\u00A5", "Chinese Yuan", Locale("zh", "CN")),
        CurrencyInfo("EGP", "EGP", "Egyptian Pound", Locale("ar", "EG")),
        CurrencyInfo("EUR", "\u20AC", "Euro", Locale("en", "DE")),
        CurrencyInfo("GBP", "\u00A3", "British Pound", Locale("en", "GB")),
        CurrencyInfo("GHS", "GH\u20B5", "Ghanaian Cedi", Locale("en", "GH")),
        CurrencyInfo("IDR", "Rp", "Indonesian Rupiah", Locale("id", "ID")),
        CurrencyInfo("INR", "\u20B9", "Indian Rupee", Locale("en", "IN")),
        CurrencyInfo("JPY", "\u00A5", "Japanese Yen", Locale("ja", "JP")),
        CurrencyInfo("KES", "KSh", "Kenyan Shilling", Locale("en", "KE")),
        CurrencyInfo("KRW", "\u20A9", "South Korean Won", Locale("ko", "KR")),
        CurrencyInfo("MXN", "MX$", "Mexican Peso", Locale("es", "MX")),
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit", Locale("ms", "MY")),
        CurrencyInfo("NGN", "\u20A6", "Nigerian Naira", Locale("en", "NG")),
        CurrencyInfo("NPR", "NPR", "Nepalese Rupee", Locale("ne", "NP")),
        CurrencyInfo("PHP", "\u20B1", "Philippine Peso", Locale("en", "PH")),
        CurrencyInfo("PKR", "PKR", "Pakistani Rupee", Locale("en", "PK")),
        CurrencyInfo("RUB", "\u20BD", "Russian Ruble", Locale("ru", "RU")),
        CurrencyInfo("SAR", "SAR", "Saudi Riyal", Locale("ar", "SA")),
        CurrencyInfo("SGD", "S$", "Singapore Dollar", Locale("en", "SG")),
        CurrencyInfo("THB", "\u0E3F", "Thai Baht", Locale("th", "TH")),
        CurrencyInfo("TRY", "\u20BA", "Turkish Lira", Locale("tr", "TR")),
        CurrencyInfo("USD", "$", "US Dollar", Locale("en", "US")),
        CurrencyInfo("VND", "\u20AB", "Vietnamese Dong", Locale("vi", "VN")),
        CurrencyInfo("ZAR", "R", "South African Rand", Locale("en", "ZA"))
    )

    private const val PREFS_NAME = "mymanager_currency"
    private const val KEY_CURRENCY_CODE = "currency_code"
    const val DEFAULT_CURRENCY = "INR"

    fun getCurrencyByCode(code: String): CurrencyInfo {
        return currencies.find { it.code == code } ?: currencies.first()
    }

    fun getDisplayLabel(currency: CurrencyInfo): String {
        return "${currency.displayName} - ${currency.code} (${currency.symbol})"
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val currency = getCurrencyByCode(currencyCode)
        val formatter = NumberFormat.getCurrencyInstance(currency.locale)
        return formatter.format(amount)
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
