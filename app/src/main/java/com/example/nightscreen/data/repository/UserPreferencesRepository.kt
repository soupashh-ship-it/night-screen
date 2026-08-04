package com.example.nightscreen.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.nightscreen.data.model.DimmerPreferences
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.data.model.ScheduleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dimmer_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val INTENSITY = floatPreferencesKey("intensity")
        val COLOR_HEX = longPreferencesKey("color_hex")
        val PRESET_ID = stringPreferencesKey("preset_id")
        val SCHEDULE_ENABLED = booleanPreferencesKey("schedule_enabled")
        val SCHEDULE_START_H = intPreferencesKey("schedule_start_h")
        val SCHEDULE_START_M = intPreferencesKey("schedule_start_m")
        val SCHEDULE_END_H = intPreferencesKey("schedule_end_h")
        val SCHEDULE_END_M = intPreferencesKey("schedule_end_m")
        val SCHEDULE_DAYS = stringPreferencesKey("schedule_days") // "1,2,3,4,5,6,7"
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val CUSTOM_PRESETS = stringPreferencesKey("custom_presets_raw") // "id:name:hex;id:name:hex"
    }

    val preferencesFlow: Flow<DimmerPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val intensity = (prefs[PreferencesKeys.INTENSITY] ?: 0.30f).coerceIn(0.05f, 0.95f)
            val colorHex = prefs[PreferencesKeys.COLOR_HEX] ?: 0xFF000000L
            val presetId = prefs[PreferencesKeys.PRESET_ID] ?: "black"
            val schedEnabled = prefs[PreferencesKeys.SCHEDULE_ENABLED] ?: false
            val schedStartH = prefs[PreferencesKeys.SCHEDULE_START_H] ?: 22
            val schedStartM = prefs[PreferencesKeys.SCHEDULE_START_M] ?: 0
            val schedEndH = prefs[PreferencesKeys.SCHEDULE_END_H] ?: 7
            val schedEndM = prefs[PreferencesKeys.SCHEDULE_END_M] ?: 0
            val daysString = prefs[PreferencesKeys.SCHEDULE_DAYS] ?: "1,2,3,4,5,6,7"
            val parsedDays = parseDaysString(daysString)

            val themeMode = prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
            val dynamicColor = prefs[PreferencesKeys.DYNAMIC_COLOR] ?: false
            val hapticsEnabled = prefs[PreferencesKeys.HAPTICS_ENABLED] ?: true
            val reduceMotion = prefs[PreferencesKeys.REDUCE_MOTION] ?: false
            val customPresetsRaw = prefs[PreferencesKeys.CUSTOM_PRESETS] ?: ""
            val parsedCustomPresets = parseCustomPresets(customPresetsRaw)

            DimmerPreferences(
                intensity = intensity,
                colorHex = colorHex,
                selectedPresetId = presetId,
                schedule = ScheduleConfig(
                    enabled = schedEnabled,
                    startHour = schedStartH,
                    startMinute = schedStartM,
                    endHour = schedEndH,
                    endMinute = schedEndM,
                    daysOfWeek = parsedDays
                ),
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                hapticsEnabled = hapticsEnabled,
                reduceMotion = reduceMotion,
                customPresets = parsedCustomPresets
            )
        }

    suspend fun updateIntensity(intensity: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.INTENSITY] = intensity.coerceIn(0.05f, 0.95f)
        }
    }

    suspend fun updateColor(colorHex: Long, presetId: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.COLOR_HEX] = colorHex
            prefs[PreferencesKeys.PRESET_ID] = presetId
        }
    }

    suspend fun updateSchedule(schedule: ScheduleConfig) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SCHEDULE_ENABLED] = schedule.enabled
            prefs[PreferencesKeys.SCHEDULE_START_H] = schedule.startHour
            prefs[PreferencesKeys.SCHEDULE_START_M] = schedule.startMinute
            prefs[PreferencesKeys.SCHEDULE_END_H] = schedule.endHour
            prefs[PreferencesKeys.SCHEDULE_END_M] = schedule.endMinute
            prefs[PreferencesKeys.SCHEDULE_DAYS] = schedule.daysOfWeek.joinToString(",")
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updateHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateReduceMotion(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.REDUCE_MOTION] = enabled
        }
    }

    suspend fun addCustomPreset(preset: FilterPreset) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[PreferencesKeys.CUSTOM_PRESETS] ?: ""
            val list = parseCustomPresets(currentRaw).toMutableList()
            list.removeAll { it.id == preset.id }
            list.add(preset)
            prefs[PreferencesKeys.CUSTOM_PRESETS] = serializeCustomPresets(list)
        }
    }

    suspend fun deleteCustomPreset(presetId: String) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[PreferencesKeys.CUSTOM_PRESETS] ?: ""
            val list = parseCustomPresets(currentRaw).filterNot { it.id == presetId }
            prefs[PreferencesKeys.CUSTOM_PRESETS] = serializeCustomPresets(list)
        }
    }

    private fun parseDaysString(raw: String): Set<Int> {
        return try {
            if (raw.isBlank()) setOf(1, 2, 3, 4, 5, 6, 7)
            else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } catch (e: Exception) {
            setOf(1, 2, 3, 4, 5, 6, 7)
        }
    }

    private fun parseCustomPresets(raw: String): List<FilterPreset> {
        if (raw.isBlank()) return emptyList()
        return try {
            raw.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size >= 3) {
                    FilterPreset(
                        id = parts[0],
                        name = parts[1],
                        colorHex = parts[2].toLongOrNull() ?: 0xFF000000L,
                        isCustom = true
                    )
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeCustomPresets(list: List<FilterPreset>): String {
        return list.joinToString(";") { "${it.id}|${it.name}|${it.colorHex}" }
    }
}
