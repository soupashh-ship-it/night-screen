package com.example.nightscreen.data.model

data class ScheduleConfig(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon .. 7=Sun
    val useSunsetSunrise: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
