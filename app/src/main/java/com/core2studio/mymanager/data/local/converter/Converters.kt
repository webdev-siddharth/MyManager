package com.core2studio.mymanager.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromMap(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toMap(value: String): Map<String, String> {
        return try {
            Json.decodeFromString<Map<String, String>>(value)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.decodeFromString<List<String>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
