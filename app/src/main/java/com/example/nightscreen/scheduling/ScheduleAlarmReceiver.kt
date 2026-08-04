package com.example.nightscreen.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.service.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = UserPreferencesRepository(context)
                val prefs = repository.preferencesFlow.first()
                val calculator = ScheduleCalculator()

                if (prefs.schedule.enabled && Settings.canDrawOverlays(context)) {
                    val cal = Calendar.getInstance()
                    val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = cal.get(Calendar.MINUTE)
                    val currentIsoDay = calculator.getIsoDayOfWeek(cal)

                    val shouldBeActive = calculator.isScheduleActiveAt(
                        currentHour, currentMinute, currentIsoDay, prefs.schedule
                    )

                    val serviceAction = if (shouldBeActive) OverlayService.ACTION_START else OverlayService.ACTION_STOP
                    val serviceIntent = Intent(context, OverlayService::class.java).apply {
                        this.action = serviceAction
                    }

                    try {
                        context.startForegroundService(serviceIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Reschedule for next trigger
                ScheduleCoordinator.scheduleNextAlarm(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
