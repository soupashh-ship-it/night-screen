package com.example.nightscreen.overlay

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

    @Synchronized
    fun showOverlay(context: Context, colorHex: Long, intensity: Float): Boolean {
        if (!Settings.canDrawOverlays(context)) {
            return false
        }

        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager) ?: return false
        windowManager = wm

        val rgbColor = extractRgbColor(colorHex)
        val alpha = touchSafetyController.computeOverlayAlpha(intensity)

        if (overlayView == null) {
            val view = View(context).apply {
                setBackgroundColor(rgbColor)
            }

            val params = WindowManager.LayoutParams().apply {
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
                    } else {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }
            }

            try {
                wm.addView(view, params)
                overlayView = view
                currentLayoutParams = params
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return updateOverlay(colorHex, intensity)
        }
    }

    @Synchronized
    fun updateOverlay(colorHex: Long, intensity: Float): Boolean {
        val view = overlayView ?: return false
        val wm = windowManager ?: return false
        val params = currentLayoutParams ?: return false

        val rgbColor = extractRgbColor(colorHex)
        val alpha = touchSafetyController.computeOverlayAlpha(intensity)

        view.setBackgroundColor(rgbColor)
        params.alpha = alpha

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

    @Synchronized
    fun hideOverlay() {
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

    private fun extractRgbColor(colorHex: Long): Int {
        val red = ((colorHex shr 16) and 0xFF).toInt()
        val green = ((colorHex shr 8) and 0xFF).toInt()
        val blue = (colorHex and 0xFF).toInt()
        return Color.rgb(red, green, blue)
    }
}
