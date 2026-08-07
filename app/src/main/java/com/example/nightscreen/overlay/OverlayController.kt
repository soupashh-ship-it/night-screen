package com.example.nightscreen.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager

class OverlayController(private val touchSafetyController: TouchSafetyController = TouchSafetyController()) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentLayoutParams: WindowManager.LayoutParams? = null
    private var currentAnimator: ValueAnimator? = null

    @Synchronized
    fun showOverlay(context: Context, colorHex: Long, intensity: Float, animate: Boolean = true): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            return false
        }

        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager) ?: return false
        windowManager = wm

        val rgbColor = extractRgbColor(colorHex)
        val targetAlpha = touchSafetyController.computeOverlayAlpha(intensity)

        if (overlayView == null) {
            val view = View(context).apply {
                setBackgroundColor(rgbColor)
                fitsSystemWindows = false
            }

            val initialAlpha = if (animate) 0.01f else targetAlpha
            val params = createLayoutParams(initialAlpha)

            try {
                wm.addView(view, params)
                overlayView = view
                currentLayoutParams = params

                if (animate) {
                    animateAlpha(0.01f, targetAlpha)
                }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return updateOverlay(colorHex, intensity, animate)
        }
    }

    fun createLayoutParams(alpha: Float): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            this.alpha = alpha

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    setFitInsetsTypes(0)
                    setFitInsetsSides(0)
                    setFitInsetsIgnoringVisibility(true)
                } else {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
    }

    @Synchronized
    fun updateOverlay(colorHex: Long, intensity: Float, animate: Boolean = false): Boolean {
        val view = overlayView ?: return false
        val wm = windowManager ?: return false
        val params = currentLayoutParams ?: return false

        val rgbColor = extractRgbColor(colorHex)
        val targetAlpha = touchSafetyController.computeOverlayAlpha(intensity)

        view.setBackgroundColor(rgbColor)

        if (animate && view.isAttachedToWindow) {
            animateAlpha(params.alpha, targetAlpha)
            return true
        } else {
            params.alpha = targetAlpha
            return try {
                if (view.isAttachedToWindow) {
                    wm.updateViewLayout(view, params)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @Synchronized
    fun hideOverlay() {
        cancelAnimator()
        val view = overlayView
        val wm = windowManager
        if (view != null && wm != null) {
            try {
                if (view.isAttachedToWindow) {
                    wm.removeViewImmediate(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
        currentLayoutParams = null
    }

    fun isOverlayActive(): Boolean {
        return overlayView != null && overlayView?.isAttachedToWindow == true
    }

    private fun animateAlpha(from: Float, to: Float) {
        cancelAnimator()
        val view = overlayView ?: return
        val wm = windowManager ?: return
        val params = currentLayoutParams ?: return

        currentAnimator = ValueAnimator.ofFloat(from, to).apply {
            duration = 250L
            addUpdateListener { anim ->
                val currentAlpha = anim.animatedValue as Float
                params.alpha = currentAlpha
                try {
                    if (view.isAttachedToWindow) {
                        wm.updateViewLayout(view, params)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            start()
        }
    }

    private fun cancelAnimator() {
        currentAnimator?.cancel()
        currentAnimator = null
    }

    private fun extractRgbColor(colorHex: Long): Int {
        val red = ((colorHex shr 16) and 0xFF).toInt()
        val green = ((colorHex shr 8) and 0xFF).toInt()
        val blue = (colorHex and 0xFF).toInt()
        return Color.rgb(red, green, blue)
    }
}
