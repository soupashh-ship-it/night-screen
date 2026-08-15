package com.example.nightscreen.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.nightscreen.overlay.OverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Opt-in accessibility service that mirrors the dimming filter with a
 * [WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY] window.
 *
 * A regular application overlay is drawn *below* the system UI, so when the
 * notification shade is expanded it sits on top of the dim layer and the
 * screen looks bright again. Accessibility overlays are drawn above the
 * status bar and the shade, which keeps the screen consistently dimmed while
 * the user toggles Wi-Fi, Bluetooth, or the brightness slider.
 *
 * The service never retrieves window content and observes no events — it only
 * re-renders a dark scrim from the shared [OverlayStateStore]. The main
 * [OverlayService] drops its own scrim while this overlay is active so the two
 * never stack, and takes back over automatically if the service is disabled.
 */
class AccessibilityDimService : AccessibilityService() {

    private val overlayController = OverlayController()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceScope.launch {
            combine(
                OverlayStateStore.isActive,
                OverlayStateStore.isPaused,
                OverlayStateStore.currentIntensity,
                OverlayStateStore.currentColor
            ) { active, paused, intensity, color ->
                if (active && !paused) {
                    val shown = overlayController.showOverlay(
                        context = this@AccessibilityDimService,
                        colorHex = color,
                        intensity = intensity,
                        animate = false,
                        windowType = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                    )
                    // Only claim dimming ownership once the window is really up,
                    // so the app overlay stays as the fallback on failure.
                    OverlayStateStore.setAccessibilityDimming(shown)
                } else {
                    overlayController.hideOverlay()
                    OverlayStateStore.setAccessibilityDimming(false)
                }
            }.collect { }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        teardown()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        teardown()
        super.onDestroy()
    }

    private fun teardown() {
        serviceScope.cancel()
        overlayController.hideOverlay()
        OverlayStateStore.setAccessibilityDimming(false)
    }
}
