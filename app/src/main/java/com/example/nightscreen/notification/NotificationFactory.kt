package com.example.nightscreen.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nightscreen.R
import com.example.nightscreen.ui.MainActivity

class NotificationFactory(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "nightscreen_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_TOGGLE_PAUSE = "com.example.nightscreen.ACTION_TOGGLE_PAUSE"
        const val ACTION_DECREASE_INTENSITY = "com.example.nightscreen.ACTION_DECREASE_INTENSITY"
        const val ACTION_INCREASE_INTENSITY = "com.example.nightscreen.ACTION_INCREASE_INTENSITY"
        const val ACTION_STOP_SERVICE = "com.example.nightscreen.ACTION_STOP_SERVICE"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notif_channel_description)
            setShowBadge(false)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }

    fun buildNotification(isActive: Boolean, isPaused: Boolean, intensity: Float): Notification {
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intensityPercent = (intensity * 100).toInt()
        val title = when {
            isPaused -> context.getString(R.string.notif_paused_title)
            isActive -> context.getString(R.string.notif_active_title)
            else -> context.getString(R.string.notif_inactive_title)
        }
        val contentText = context.getString(R.string.notif_intensity, intensityPercent)

        val pauseResumeActionText = if (isPaused) {
            context.getString(R.string.notif_action_resume)
        } else {
            context.getString(R.string.notif_action_pause)
        }
        val pauseResumePendingIntent = createBroadcastPendingIntent(ACTION_TOGGLE_PAUSE, 1)
        val decreasePendingIntent = createBroadcastPendingIntent(ACTION_DECREASE_INTENSITY, 2)
        val increasePendingIntent = createBroadcastPendingIntent(ACTION_INCREASE_INTENSITY, 3)
        val stopPendingIntent = createBroadcastPendingIntent(ACTION_STOP_SERVICE, 4)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_qs_dimmer)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(isActive && !isPaused)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_qs_dimmer, pauseResumeActionText, pauseResumePendingIntent)
            .addAction(R.drawable.ic_qs_dimmer, context.getString(R.string.notif_action_decrease), decreasePendingIntent)
            .addAction(R.drawable.ic_qs_dimmer, context.getString(R.string.notif_action_increase), increasePendingIntent)
            .addAction(R.drawable.ic_qs_dimmer, context.getString(R.string.notif_action_stop), stopPendingIntent)

        return builder.build()
    }

    private fun createBroadcastPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
