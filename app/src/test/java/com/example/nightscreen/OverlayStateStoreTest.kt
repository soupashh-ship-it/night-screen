package com.example.nightscreen

import com.example.nightscreen.service.OverlayStateStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OverlayStateStoreTest {

    @Test
    fun `OverlayStateStore updateState updates state flow values`() {
        OverlayStateStore.updateState(active = true, paused = false, intensity = 0.45f, color = 0xFFFF9800L)

        assertTrue(OverlayStateStore.isActive.value)
        assertFalse(OverlayStateStore.isPaused.value)
        assertEquals(0.45f, OverlayStateStore.currentIntensity.value, 0.001f)
        assertEquals(0xFFFF9800L, OverlayStateStore.currentColor.value)

        // Pause state update
        OverlayStateStore.updateState(active = true, paused = true)
        assertTrue(OverlayStateStore.isActive.value)
        assertTrue(OverlayStateStore.isPaused.value)

        // Stop state update
        OverlayStateStore.updateState(active = false, paused = false)
        assertFalse(OverlayStateStore.isActive.value)
        assertFalse(OverlayStateStore.isPaused.value)
    }

    @Test
    fun `OverlayStateStore clamps intensity`() {
        OverlayStateStore.updateState(active = true, intensity = 1.5f)
        assertEquals(0.95f, OverlayStateStore.currentIntensity.value, 0.001f)

        OverlayStateStore.updateState(active = true, intensity = 0.01f)
        assertEquals(0.05f, OverlayStateStore.currentIntensity.value, 0.001f)
    }

    @Test
    fun `OverlayStateStore persists paused state to preferences`() {
        val context = RuntimeEnvironment.getApplication()
        OverlayStateStore.init(context)
        OverlayStateStore.updateState(active = true, paused = true)
        assertTrue(OverlayStateStore.isPaused.value)

        val prefs = context.getSharedPreferences(OverlayStateStore.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        assertTrue(prefs.getBoolean("is_paused", false))

        OverlayStateStore.updateState(active = false, paused = false)
        assertFalse(prefs.getBoolean("is_paused", true))
    }
}
