package com.example.nightscreen

import com.example.nightscreen.service.OverlayStateStore
import org.junit.Assert.*
import org.junit.Test

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
}
