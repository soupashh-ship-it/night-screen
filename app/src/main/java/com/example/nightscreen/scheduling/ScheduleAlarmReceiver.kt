package com.example.nightscreen.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.nightscreen.NightScreenApp
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.service.OverlayService
import com.example.nightscreen.service.OverlayStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Receives the AlarmManager trigger for the dimming schedule and delegates the
 * work to [ScheduleWorker] so it survives the receiver's short lifetime.
 */
class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scope = (appContext as? NightScreenApp)?.applicationScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch(Dispatchers.IO) {
            try {
                val prefs = UserPreferencesRepository(appContext).preferencesFlow.first()
                if (prefs.schedule.enabled && Settings.canDrawOverlays(appContext)) {
                    val cal = Calendar.getInstance()
                    val calculator = ScheduleCalculator()
                    val shouldBeActive = calculator.isScheduleActiveAt(
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        calculator.getIsoDayOfWeek(cal),
                        prefs.schedule
                    )
                    
                    val isActive = OverlayStateStore.isActive.value
                    val isPaused = OverlayStateStore.isPaused.value
                    val currentlyActive = isActive && !isPaused
                    
                    if (shouldBeActive != currentlyActive) {
                        // B5: Do not force restart if user explicitly paused it
                        if (!(shouldBeActive && isActive && isPaused)) {
                            val serviceIntent = Intent(appContext, OverlayService::class.java).apply {
                                action = if (shouldBeActive) OverlayService.ACTION_START else OverlayService.ACTION_STOP
                            }
                            try {
                                if (shouldBeActive) {
                                    ContextCompat.startForegroundService(appContext, serviceIntent)
                                } else {
                                    appContext.stopService(serviceIntent)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } finally {
                ScheduleCoordinator.refreshSchedule(appContext)
                pendingResult.finish()
            }
        }
    }
}
