package com.example.nightscreen.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.nightscreen.service.OverlayService
import com.example.nightscreen.service.OverlayStateStore
import com.example.nightscreen.ui.MainActivity

class DimmerTileService : TileService() {

    companion object {
        fun requestListeningState(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, DimmerTileService::class.java)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("navigate_to", "settings")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @SuppressLint("StartActivityAndCollapseDeprecated")
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val isActive = OverlayStateStore.isActive.value
        val action = if (isActive) OverlayService.ACTION_STOP else OverlayService.ACTION_START
        val serviceIntent = Intent(this, OverlayService::class.java).apply {
            this.action = action
        }

        try {
            startForegroundService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        if (!Settings.canDrawOverlays(this)) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Night Screen"
            setSubtitleSafe("Permission Needed")
            tile.updateTile()
            return
        }

        val isActive = OverlayStateStore.isActive.value
        val isPaused = OverlayStateStore.isPaused.value

        when {
            isActive && !isPaused -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Night Screen"
                setSubtitleSafe("On")
            }
            isActive && isPaused -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Night Screen"
                setSubtitleSafe("Paused")
            }
            else -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Night Screen"
                setSubtitleSafe("Off")
            }
        }
        tile.updateTile()
    }

    private fun setSubtitleSafe(subtitle: String) {
        // Tile#setSubtitle requires API 29.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile?.subtitle = subtitle
        }
    }
}
