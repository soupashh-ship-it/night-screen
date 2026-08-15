package com.example.nightscreen.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nightscreen.data.model.DimmerPreferences
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.service.OverlayService
import com.example.nightscreen.service.OverlayStateStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Runs on every schedule event — an alarm firing, a boot, a time change, or a
 * schedule edit — and aligns the app with the persisted schedule: applies the
 * current window (start/stop the dimmer) and (re)arms the next
 * [ScheduleAlarmReceiver] trigger.
 *
 * Evaluating on boot and schedule edits (not just when an alarm fires) closes
 * the gap where the schedule was active but the dimmer stayed off — e.g. after
 * a reboot during an active window, or when enabling a schedule mid-window.
 *
 * Runs inside WorkManager so the work survives the broadcast receiver that
 * requested it — the system may kill the process right after `onReceive()`
 * returns, which previously dropped reschedules.
 */
class ScheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val prefs = UserPreferencesRepository(context).preferencesFlow.first()
            applyScheduleNow(context, prefs)
            rearmAlarm(context, prefs)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Log rather than retry: a retry loop would not help here, and the
            // next boot / time change / schedule edit re-enqueues this work.
            e.printStackTrace()
            Result.success()
        }
    }

    /** Aligns the dimmer with the schedule at this moment. */
    private fun applyScheduleNow(context: Context, prefs: DimmerPreferences) {
        if (!prefs.schedule.enabled || !Settings.canDrawOverlays(context)) return

        val cal = Calendar.getInstance()
        val calculator = ScheduleCalculator()
        val shouldBeActive = calculator.isScheduleActiveAt(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            calculator.getIsoDayOfWeek(cal),
            prefs.schedule
        )
        val currentlyActive = OverlayStateStore.isActive.value && !OverlayStateStore.isPaused.value
        if (shouldBeActive == currentlyActive) return

        val serviceIntent = Intent(context, OverlayService::class.java).apply {
            action = if (shouldBeActive) OverlayService.ACTION_START else OverlayService.ACTION_STOP
        }
        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** (Re)arm the next alarm trigger, or cancel it when the schedule is disabled. */
    private fun rearmAlarm(context: Context, prefs: DimmerPreferences) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SCHEDULE,
            Intent(context, ScheduleAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!prefs.schedule.enabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val nextAction = ScheduleCalculator().getNextTrigger(System.currentTimeMillis(), prefs.schedule)
        if (nextAction != null) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextAction.targetTimeMillis,
                pendingIntent
            )
        }
    }

    companion object {
        private const val REQUEST_CODE_SCHEDULE = 2001
    }
}
