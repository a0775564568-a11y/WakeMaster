package com.example.data.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            data.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
