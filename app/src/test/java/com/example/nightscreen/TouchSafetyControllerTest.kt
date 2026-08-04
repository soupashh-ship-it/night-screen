package com.example.nightscreen

import com.example.nightscreen.overlay.TouchSafetyController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TouchSafetyControllerTest {

    private lateinit var controller: TouchSafetyController

    @Before
    fun setUp() {
        controller = TouchSafetyController(null) // null context falls back to DEFAULT_MAX_OBSCURING_OPACITY (0.80f)
    }

    @Test
    fun `computeOverlayAlpha clamps minimum intensity`() {
        val alpha = controller.computeOverlayAlpha(0.01f)
        assertEquals(TouchSafetyController.MIN_ALPHA, alpha, 0.001f)
    }

    @Test
    fun `computeOverlayAlpha does not exceed safe maximum opacity`() {
        val maxOpacity = controller.getMaxObscuringOpacity()
        val safeMax = maxOpacity - TouchSafetyController.SAFETY_MARGIN

        val alphaAtMax = controller.computeOverlayAlpha(1.0f)
        assertTrue("Alpha should not exceed safe maximum", alphaAtMax <= safeMax)
        assertEquals(safeMax, alphaAtMax, 0.001f)
    }

    @Test
    fun `computeOverlayAlpha scales linearly between min and safeMax`() {
        val minAlpha = controller.computeOverlayAlpha(0.05f)
        val midAlpha = controller.computeOverlayAlpha(0.50f)
        val maxAlpha = controller.computeOverlayAlpha(0.95f)

        assertTrue(minAlpha < midAlpha)
        assertTrue(midAlpha < maxAlpha)
    }
}
