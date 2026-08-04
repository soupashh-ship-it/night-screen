package com.example.nightscreen

import com.example.nightscreen.data.model.DimmerPreferences
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.data.model.ScheduleConfig
import org.junit.Assert.*
import org.junit.Test

class DimmerPreferencesTest {

    @Test
    fun `default DimmerPreferences has expected default values`() {
        val prefs = DimmerPreferences()
        assertEquals(0.30f, prefs.intensity, 0.001f)
        assertEquals(0xFF000000L, prefs.colorHex)
        assertEquals("black", prefs.selectedPresetId)
        assertEquals("SYSTEM", prefs.themeMode)
        assertFalse(prefs.dynamicColor)
        assertTrue(prefs.hapticsEnabled)
        assertFalse(prefs.reduceMotion)
        assertTrue(prefs.customPresets.isEmpty())
    }

    @Test
    fun `FilterPreset DEFAULT_PRESETS contains 4 built-in presets`() {
        val presets = FilterPreset.DEFAULT_PRESETS
        assertEquals(4, presets.size)
        assertEquals("black", presets[0].id)
        assertEquals("amber", presets[1].id)
        assertEquals("orange", presets[2].id)
        assertEquals("red", presets[3].id)
    }

    @Test
    fun `ScheduleConfig defaults to 7 days active`() {
        val config = ScheduleConfig()
        assertFalse(config.enabled)
        assertEquals(22, config.startHour)
        assertEquals(7, config.endHour)
        assertEquals(7, config.daysOfWeek.size)
    }
}
