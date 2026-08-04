package com.example.nightscreen

import com.example.nightscreen.ui.screens.PreviewThrottle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewThrottleTest {

    @Test
    fun `first preview is always allowed`() {
        val throttle = PreviewThrottle()
        assertTrue(throttle.shouldSend(1000L))
    }

    @Test
    fun `previews inside the window are suppressed`() {
        val throttle = PreviewThrottle(minIntervalMs = 60L)
        assertTrue(throttle.shouldSend(1000L))
        assertFalse(throttle.shouldSend(1040L)) // 40ms later — inside window
        assertFalse(throttle.shouldSend(1059L)) // 59ms later — inside window
    }

    @Test
    fun `preview after the window is allowed`() {
        val throttle = PreviewThrottle(minIntervalMs = 60L)
        assertTrue(throttle.shouldSend(1000L))
        assertTrue(throttle.shouldSend(1060L)) // exactly 60ms — allowed
    }

    @Test
    fun `time moving backwards does not break the window`() {
        val throttle = PreviewThrottle(minIntervalMs = 60L)
        assertTrue(throttle.shouldSend(1000L))
        // Clock correction (e.g. NTP) must not re-open the window indefinitely.
        assertFalse(throttle.shouldSend(500L))
        assertFalse(throttle.shouldSend(600L))
        assertTrue(throttle.shouldSend(1500L))
    }

    @Test
    fun `reset reopens the window`() {
        val throttle = PreviewThrottle(minIntervalMs = 60L)
        assertTrue(throttle.shouldSend(1000L))
        assertFalse(throttle.shouldSend(1030L))
        throttle.reset()
        assertTrue(throttle.shouldSend(1040L))
    }
}
