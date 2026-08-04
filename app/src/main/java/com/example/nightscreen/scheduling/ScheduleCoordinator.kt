package com.example.nightscreen.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nightscreen.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ScheduleCoordinator {

    private const val REQUEST_CODE_SCHEDULE = 2001

    fun scheduleNextAlarm(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = UserPreferencesRepository(appContext)
                val prefs = repository.preferencesFlow.first()
                val calculator = ScheduleCalculator()
                val nowMillis = System.currentTimeMillis()

                val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@launch
                val intent = Intent(appContext, ScheduleAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    appContext,
                    REQUEST_CODE_SCHEDULE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (!prefs.schedule.enabled) {
                    alarmManager.cancel(pendingIntent)
                    return@launch
                }

                val nextAction = calculator.getNextTrigger(nowMillis, prefs.schedule)
                if (nextAction != null) {
                    val triggerTime = nextAction.targetTimeMillis
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
