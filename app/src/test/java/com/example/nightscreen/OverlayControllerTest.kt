package com.example.nightscreen

import android.view.WindowManager
import com.example.nightscreen.overlay.OverlayController
import com.example.nightscreen.overlay.TouchSafetyController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OverlayControllerTest {

    private lateinit var controller: OverlayController

    @Before
    fun setUp() {
        controller = OverlayController(TouchSafetyController(null))
    }

    @Test
    fun `createLayoutParams configures overlay window type and flags`() {
        val params = controller.createLayoutParams(0.5f)
        assertNotNull(params)
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, params.width)
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, params.height)

        val expectedFlags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        assertEquals(expectedFlags, params.flags and expectedFlags)
        assertEquals(0.5f, params.alpha, 0.001f)
    }

    @Test
    fun `createLayoutParams configures display cutout mode`() {
        val params = controller.createLayoutParams(0.4f)
        assertEquals(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS, params.layoutInDisplayCutoutMode)
    }
}
