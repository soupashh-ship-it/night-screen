package com.example.nightscreen

import com.example.nightscreen.data.model.FilterPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ColorTemperatureTest {

    @Test
    fun `kelvinToColorHex produces warm red at 1500K`() {
        val colorHex = FilterPreset.kelvinToColorHex(1500)
        assertNotNull(colorHex)
        val red = ((colorHex shr 16) and 0xFF).toInt()
        val blue = (colorHex and 0xFF).toInt()

        assertEquals(255, red)
        assert(blue < 50)
    }

    @Test
    fun `kelvinToColorHex produces balanced warm white at 4500K`() {
        val colorHex = FilterPreset.kelvinToColorHex(4500)
        assertNotNull(colorHex)
        val red = ((colorHex shr 16) and 0xFF).toInt()
        val green = ((colorHex shr 8) and 0xFF).toInt()
        val blue = (colorHex and 0xFF).toInt()

        assertEquals(255, red)
        assert(green in 150..255)
        assert(blue in 100..255)
    }
}
