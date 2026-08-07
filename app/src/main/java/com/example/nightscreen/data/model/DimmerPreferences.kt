package com.example.nightscreen.data.model

data class DimmerPreferences(
    val intensity: Float = 0.30f,
    val colorHex: Long = 0xFF000000L,
    val selectedPresetId: String = "black",
    val schedule: ScheduleConfig = ScheduleConfig(),
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val dynamicColor: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val reduceMotion: Boolean = false,
    val customPresets: List<FilterPreset> = emptyList(),
    val syncHardwareBrightness: Boolean = false,
    val autoBatterySaver: Boolean = false,
    val batteryThreshold: Int = 15
)
