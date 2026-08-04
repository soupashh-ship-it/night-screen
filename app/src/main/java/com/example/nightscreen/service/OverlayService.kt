package com.example.nightscreen.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.notification.NotificationFactory
import com.example.nightscreen.overlay.OverlayController
import com.example.nightscreen.tile.DimmerTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.example.nightscreen.ACTION_START"
        const val ACTION_STOP = "com.example.nightscreen.ACTION_STOP"
        const val ACTION_PAUSE = "com.example.nightscreen.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.nightscreen.ACTION_RESUME"
        const val ACTION_UPDATE = "com.example.nightscreen.ACTION_UPDATE"

        const val EXTRA_INTENSITY = "extra_intensity"
        const val EXTRA_COLOR_HEX = "extra_color_hex"
    }

    private val overlayController by lazy { OverlayController() }
    private val notificationFactory by lazy { NotificationFactory(this) }
    private val preferencesRepository by lazy { UserPreferencesRepository(this) }
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START, ACTION_RESUME -> handleStartOrResume(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_STOP -> handleStop()
            ACTION_UPDATE -> handleUpdate(intent)
        }

        return START_NOT_STICKY
    }

    private fun handleStartOrResume(intent: Intent?) {
        serviceScope.launch {
            val prefs = preferencesRepository.preferencesFlow.first()
            val intensity = intent?.getFloatExtra(EXTRA_INTENSITY, -1f)?.takeIf { it >= 0 } ?: prefs.intensity
            val colorHex = intent?.getLongExtra(EXTRA_COLOR_HEX, -1L)?.takeIf { it != -1L } ?: prefs.colorHex

            val notification = notificationFactory.buildNotification(isActive = true, isPaused = false, intensity = intensity)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NotificationFactory.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                @Suppress("DEPRECATION")
                startForeground(NotificationFactory.NOTIFICATION_ID, notification)
            }

            val success = overlayController.showOverlay(this@OverlayService, colorHex, intensity)
            if (success) {
                OverlayStateStore.updateState(active = true, paused = false, intensity = intensity, color = colorHex)
            } else {
                // If overlay permission was lost or window failed, stop service
                handleStop()
            }
            DimmerTileService.requestListeningState(this@OverlayService)
        }
    }

    private fun handlePause() {
        overlayController.hideOverlay()
        val intensity = OverlayStateStore.currentIntensity.value
        OverlayStateStore.updateState(active = true, paused = true)

        val notification = notificationFactory.buildNotification(isActive = true, isPaused = true, intensity = intensity)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.notify(NotificationFactory.NOTIFICATION_ID, notification)

        DimmerTileService.requestListeningState(this)
    }

    private fun handleUpdate(intent: Intent?) {
        val hasIntensityExtra = intent?.hasExtra(EXTRA_INTENSITY) == true
        val hasColorExtra = intent?.hasExtra(EXTRA_COLOR_HEX) == true

        if (hasIntensityExtra || hasColorExtra) {
            // Cheap path: values ride in the intent, no DataStore read needed.
            val intensity = intent?.getFloatExtra(EXTRA_INTENSITY, -1f)
                ?.takeIf { it >= 0f } ?: OverlayStateStore.currentIntensity.value
            val colorHex = intent?.getLongExtra(EXTRA_COLOR_HEX, -1L)
                ?.takeIf { it != -1L } ?: OverlayStateStore.currentColor.value
            applyUpdate(intensity, colorHex)
        } else {
            serviceScope.launch {
                val prefs = preferencesRepository.preferencesFlow.first()
                applyUpdate(prefs.intensity, prefs.colorHex)
            }
        }
    }

    private fun applyUpdate(intensity: Float, colorHex: Long) {
        if (OverlayStateStore.isActive.value && !OverlayStateStore.isPaused.value) {
            overlayController.updateOverlay(colorHex, intensity)
            OverlayStateStore.updateState(active = true, paused = false, intensity = intensity, color = colorHex)

            val notification = notificationFactory.buildNotification(isActive = true, isPaused = false, intensity = intensity)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(NotificationFactory.NOTIFICATION_ID, notification)
        }
    }

    private fun handleStop() {
        overlayController.hideOverlay()
        OverlayStateStore.updateState(active = false, paused = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        DimmerTileService.requestListeningState(this)
    }

    override fun onDestroy() {
        overlayController.hideOverlay()
        OverlayStateStore.updateState(active = false, paused = false)
        DimmerTileService.requestListeningState(this)
        super.onDestroy()
    }
}
