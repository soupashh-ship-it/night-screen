package com.example.nightscreen.overlay

import android.content.Context
import android.hardware.input.InputManager
import android.os.Build

class TouchSafetyController(private val context: Context? = null) {

    companion object {
        const val DEFAULT_MAX_OBSCURING_OPACITY = 0.80f
        const val SAFETY_MARGIN = 0.05f
        const val MIN_ALPHA = 0.05f
    }

    /**
     * Gets the system maximum obscuring opacity allowed for untrusted touch-through windows.
     * On Android 12 (API 31+), queries InputManager.maximumObscuringOpacityForTouch.
     */
    fun getMaxObscuringOpacity(): Float {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
            return try {
                val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager
                val maxOpacity = inputManager?.maximumObscuringOpacityForTouch ?: DEFAULT_MAX_OBSCURING_OPACITY
                if (maxOpacity in 0.1f..1.0f) maxOpacity else DEFAULT_MAX_OBSCURING_OPACITY
            } catch (e: Exception) {
                DEFAULT_MAX_OBSCURING_OPACITY
            }
        }
        return DEFAULT_MAX_OBSCURING_OPACITY
    }

    /**
     * Computes the final layout alpha for the WindowManager overlay view based on user intensity.
     * Guarantees alpha never exceeds (maxObscuringOpacity - SAFETY_MARGIN) to prevent blocking touches.
     */
    fun computeOverlayAlpha(userIntensity: Float): Float {
        val clampedIntensity = userIntensity.coerceIn(MIN_ALPHA, 0.95f)
        val maxSystemOpacity = getMaxObscuringOpacity()
        val safeMaxAlpha = (maxSystemOpacity - SAFETY_MARGIN).coerceIn(0.10f, 0.90f)

        // Linear interpolation from MIN_ALPHA (0.05f) to safeMaxAlpha based on clampedIntensity
        val normalizedIntensity = (clampedIntensity - MIN_ALPHA) / (0.95f - MIN_ALPHA)
        val alpha = MIN_ALPHA + (normalizedIntensity * (safeMaxAlpha - MIN_ALPHA))
        return alpha.coerceIn(MIN_ALPHA, safeMaxAlpha)
    }
}
