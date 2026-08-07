package com.example.nightscreen.data.model

import kotlin.math.ln
import kotlin.math.pow

data class FilterPreset(
    val id: String,
    val name: String,
    val colorHex: Long,
    val isCustom: Boolean = false,
    val kelvin: Int? = null
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            FilterPreset("black", "Neutral Black", 0xFF000000L),
            FilterPreset("bedtime", "Bedtime 2000K", kelvinToColorHex(2000), kelvin = 2000),
            FilterPreset("amber", "Warm Amber", 0xFFFF9800L, kelvin = 2700),
            FilterPreset("orange", "Soft Orange", 0xFFFF5722L),
            FilterPreset("astronomy", "Astronomy Red", 0xFFFF0000L),
            FilterPreset("red", "Deep Red", 0xFFD32F2FL)
        )

        /**
         * Calculates approximate RGB hex color from Kelvin color temperature (1000K to 10000K).
         */
        fun kelvinToColorHex(kelvin: Int): Long {
            val temp = (kelvin.coerceIn(1000, 10000) / 100.0)

            // Red
            val red = if (temp <= 66) {
                255
            } else {
                val r = 329.698727446 * ((temp - 60).pow(-0.1332047592))
                r.coerceIn(0.0, 255.0).toInt()
            }

            // Green
            val green = if (temp <= 66) {
                val g = 99.4708025861 * ln(temp) - 161.1195681661
                g.coerceIn(0.0, 255.0).toInt()
            } else {
                val g = 288.1221695283 * ((temp - 60).pow(-0.0755148492))
                g.coerceIn(0.0, 255.0).toInt()
            }

            // Blue
            val blue = if (temp >= 66) {
                255
            } else if (temp <= 19) {
                0
            } else {
                val b = 138.5177312231 * ln(temp - 10) - 305.0447927307
                b.coerceIn(0.0, 255.0).toInt()
            }

            return (0xFFL shl 24) or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong()
        }
    }
}
