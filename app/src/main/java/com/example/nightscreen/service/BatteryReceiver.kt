package com.example.nightscreen.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.example.nightscreen.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BatteryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BATTERY_LOW || action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level < 0 || scale <= 0) return

            val pct = (level * 100) / scale
            val repo = UserPreferencesRepository(context)

            CoroutineScope(Dispatchers.Main).launch {
                val prefs = repo.preferencesFlow.first()
                if (prefs.autoBatterySaver) {
                    if (pct <= prefs.batteryThreshold && !OverlayStateStore.isActive.value) {
                        val startIntent = Intent(context, OverlayService::class.java).apply {
                            this.action = OverlayService.ACTION_START
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(startIntent)
                        } else {
                            context.startService(startIntent)
                        }
                    }
                }
            }
        }
    }
}
