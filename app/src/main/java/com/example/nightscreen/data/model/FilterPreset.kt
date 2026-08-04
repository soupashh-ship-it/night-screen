package com.example.nightscreen.data.model

data class FilterPreset(
    val id: String,
    val name: String,
    val colorHex: Long,
    val isCustom: Boolean = false
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            FilterPreset("black", "Neutral Black", 0xFF000000L),
            FilterPreset("amber", "Warm Amber", 0xFFFF9800L),
            FilterPreset("orange", "Soft Orange", 0xFFFF5722L),
            FilterPreset("red", "Deep Red", 0xFFD32F2FL)
        )
    }
}
