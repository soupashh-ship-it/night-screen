package com.example.nightscreen

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.example.nightscreen.service.BatteryReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * App entry point. Registers the battery monitor at runtime because
 * manifest-declared receivers for `ACTION_BATTERY_CHANGED` / `ACTION_BATTERY_LOW`
 * are not reliably delivered on Android 8.0+ (neither is on the implicit-broadcast
 * exemption list). `BATTERY_CHANGED` is sticky, so registration also delivers the
 * current battery state immediately — the auto-battery-saver check runs on app
 * start without waiting for a level change.
 */
class NightScreenApp : Application() {

    /** App-lifetime scope for short background tasks (e.g. battery auto-start). */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val batteryReceiver = BatteryReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
        }
        ContextCompat.registerReceiver(
            this,
            batteryReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}
