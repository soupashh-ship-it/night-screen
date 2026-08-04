package com.example.nightscreen.ui.screens

/**
 * Rate-limits live overlay previews during a slider drag.
 *
 * The composable calls [shouldSend] on every drag tick; only the first call in
 * each [minIntervalMs] window is forwarded to the service. Pure Kotlin so the
 * debounce behaviour is unit-testable without Android dependencies.
 */
class PreviewThrottle(private val minIntervalMs: Long = 60L) {

    private var lastPreviewAt = Long.MIN_VALUE

    /** Returns true if a preview should be sent at [nowMs] and records it. */
    fun shouldSend(nowMs: Long): Boolean {
        if (lastPreviewAt == Long.MIN_VALUE || nowMs - lastPreviewAt >= minIntervalMs) {
            lastPreviewAt = nowMs
            return true
        }
        return false
    }

    fun reset() {
        lastPreviewAt = Long.MIN_VALUE
    }
}
