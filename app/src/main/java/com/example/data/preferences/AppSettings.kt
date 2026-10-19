package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wakemaster_prefs")

class AppSettings(private val context: Context) {

    companion object {
        val KEY_SETUP_WIZARD_COMPLETED = booleanPreferencesKey("setup_wizard_completed")
        val KEY_THEME = stringPreferencesKey("app_theme") // SYSTEM, LIGHT, DARK
        val KEY_DEFAULT_MISSION_TYPE = stringPreferencesKey("default_mission_type")
        val KEY_DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
        val KEY_DEFAULT_MISSION_COUNT = intPreferencesKey("default_mission_count")
        val KEY_DEFAULT_SNOOZE_MINUTES = intPreferencesKey("default_snooze_minutes")
        val KEY_DEFAULT_MAX_RING_MINUTES = intPreferencesKey("default_max_ring_minutes")
        val KEY_DEFAULT_GRADUAL_VOLUME = booleanPreferencesKey("default_gradual_volume")
        val KEY_DEFAULT_VOLUME = floatPreferencesKey("default_volume")
        val KEY_DEFAULT_LOCK_TASK = booleanPreferencesKey("default_lock_task")
        val KEY_UNINSTALL_PROTECTION = booleanPreferencesKey("uninstall_protection")
    }

    val isSetupWizardCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SETUP_WIZARD_COMPLETED] ?: false
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "SYSTEM"
    }

    val defaultMissionType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_MISSION_TYPE] ?: "MATH"
    }

    val defaultDifficulty: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_DIFFICULTY] ?: "MEDIUM"
    }

    val defaultMissionCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_MISSION_COUNT] ?: 1
    }

    val defaultSnoozeMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_SNOOZE_MINUTES] ?: 5
    }

    val defaultMaxRingMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_MAX_RING_MINUTES] ?: 10
    }

    val defaultGradualVolume: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_GRADUAL_VOLUME] ?: true
    }

    val defaultVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_VOLUME] ?: 0.9f
    }

    val defaultLockTask: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_LOCK_TASK] ?: true
    }

    val uninstallProtection: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_UNINSTALL_PROTECTION] ?: false
    }

    suspend fun setSetupWizardCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SETUP_WIZARD_COMPLETED] = completed
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun updateDefaults(
        missionType: String,
        difficulty: String,
        missionCount: Int,
        snoozeMinutes: Int,
        maxRingMinutes: Int,
        gradualVolume: Boolean,
        volume: Float,
        lockTask: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_MISSION_TYPE] = missionType
            preferences[KEY_DEFAULT_DIFFICULTY] = difficulty
            preferences[KEY_DEFAULT_MISSION_COUNT] = missionCount
            preferences[KEY_DEFAULT_SNOOZE_MINUTES] = snoozeMinutes
            preferences[KEY_DEFAULT_MAX_RING_MINUTES] = maxRingMinutes
            preferences[KEY_DEFAULT_GRADUAL_VOLUME] = gradualVolume
            preferences[KEY_DEFAULT_VOLUME] = volume
            preferences[KEY_DEFAULT_LOCK_TASK] = lockTask
        }
    }

    suspend fun setUninstallProtection(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_UNINSTALL_PROTECTION] = enabled
        }
    }
}
