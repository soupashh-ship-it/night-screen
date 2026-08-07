package com.example.nightscreen

import com.example.nightscreen.scheduling.SunsetSunriseCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SunsetSunriseCalculatorTest {

    @Test
    fun `calculateSolarTimes returns default times for zero coordinates`() {
        val times = SunsetSunriseCalculator.calculateSolarTimes(Calendar.getInstance(), 0.0, 0.0)
        assertNotNull(times)
        assertEquals(6, times.sunriseHour)
        assertEquals(30, times.sunriseMinute)
        assertEquals(18, times.sunsetHour)
        assertEquals(30, times.sunsetMinute)
    }

    @Test
    fun `calculateSolarTimes returns valid hour ranges for realistic coordinates`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).apply {
            set(2026, Calendar.JUNE, 21)
        }
        val times = SunsetSunriseCalculator.calculateSolarTimes(cal, 40.7128, -74.0060)
        assertNotNull(times)
        assert(times.sunriseHour in 0..23)
        assert(times.sunsetHour in 0..23)
    }
}
