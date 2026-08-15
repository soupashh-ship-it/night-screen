package com.example.nightscreen.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.example.nightscreen.NightScreenApp
import com.example.nightscreen.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Runtime-registered battery monitor (see [NightScreenApp]).
 *
 * Manifest registration for `ACTION_BATTERY_CHANGED` / `ACTION_BATTERY_LOW` is
 * not guaranteed on Android 8.0+ (they are not on the implicit-broadcast
 * exemption list), so this receiver is registered dynamically while the app
 * process is alive. Because `BATTERY_CHANGED` is sticky, the current battery
 * level is also delivered immediately on registration.
 */
class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BATTERY_LOW && action != Intent.ACTION_BATTERY_CHANGED) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return
        val pct = (level * 100) / scale

        val app = context.applicationContext as? NightScreenApp ?: return
        app.applicationScope.launch {
            val prefs = UserPreferencesRepository(context).preferencesFlow.first()
            if (!prefs.autoBatterySaver || pct > prefs.batteryThreshold || OverlayStateStore.isActive.value) {
                return@launch
            }

            val startIntent = Intent(context, OverlayService::class.java).apply {
                this.action = OverlayService.ACTION_START
            }
            try {
                context.startForegroundService(startIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
