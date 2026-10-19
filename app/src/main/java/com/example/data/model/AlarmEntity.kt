package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "שעון מעורר",
    val isEnabled: Boolean = true,
    val daysOfWeek: List<Int> = emptyList(), // 1=Sunday, 2=Monday ... 7=Saturday. Empty = One-time
    val ringtoneUri: String = "DEFAULT",
    val ringtoneTitle: String = "שעון ברירת מחדל",
    val vibrate: Boolean = true,
    val vibrationPattern: String = "STANDARD", // STANDARD, HEAVY, PULSE, ESCALATING
    val volume: Float = 0.9f,
    val gradualVolume: Boolean = true, // Gradual crescendo over 30s
    val missionType: String = "MATH", // MATH, PUZZLE, MEMORY, SEQUENCE, SHAPES, TYPING, RANDOM
    val missionDifficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val missionCount: Int = 1, // 1 to 5 consecutive missions
    val snoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 5,
    val maxRingingMinutes: Int = 10,
    val lockTaskMode: Boolean = true, // Mission Lock Mode
    val emergencyStopAllowed: Boolean = true
) {
    fun formatTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }

    fun getDaysSummaryHebrew(): String {
        if (daysOfWeek.isEmpty()) {
            return "חד פעמי"
        }
        if (daysOfWeek.size == 7) {
            return "כל יום"
        }
        val weekdays = listOf(1, 2, 3, 4, 5) // Sun - Thu
        if (daysOfWeek.size == 5 && daysOfWeek.containsAll(weekdays)) {
            return "ימי חול (א׳-ה׳)"
        }
        val weekend = listOf(6, 7) // Fri - Sat
        if (daysOfWeek.size == 2 && daysOfWeek.containsAll(weekend)) {
            return "סוף שבוע (ו׳-ש׳)"
        }
        val dayNames = mapOf(
            1 to "א׳",
            2 to "ב׳",
            3 to "ג׳",
            4 to "ד׳",
            5 to "ה׳",
            6 to "ו׳",
            7 to "ש׳"
        )
        return daysOfWeek.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }

    fun getMissionNameHebrew(): String {
        return when (missionType) {
            "MATH" -> "תרגילי חשבון"
            "PUZZLE" -> "פאזל תמונה"
            "MEMORY" -> "משחק זיכרון"
            "SEQUENCE" -> "סדרות מספרים"
            "SHAPES" -> "התאמת צורות"
            "TYPING" -> "הקלדת משפט"
            "RANDOM" -> "משימה אקראית"
            else -> "משימת השכמה"
        }
    }

    fun getDifficultyHebrew(): String {
        return when (missionDifficulty) {
            "EASY" -> "קל"
            "MEDIUM" -> "בינוני"
            "HARD" -> "מאתגר"
            else -> "בינוני"
        }
    }
}
