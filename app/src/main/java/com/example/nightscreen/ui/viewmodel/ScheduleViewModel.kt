package com.example.nightscreen.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nightscreen.R
import com.example.nightscreen.data.model.ScheduleConfig
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.scheduling.ScheduleCalculator
import com.example.nightscreen.scheduling.ScheduleCoordinator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)
    private val calculator = ScheduleCalculator()

    val scheduleConfig: StateFlow<ScheduleConfig> = repository.preferencesFlow
        .map { it.schedule }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleConfig())

    fun updateSchedule(context: Context, newConfig: ScheduleConfig) {
        viewModelScope.launch {
            repository.updateSchedule(newConfig)
            ScheduleCoordinator.scheduleNextAlarm(context)
        }
    }

    fun toggleDay(context: Context, dayIso: Int) {
        val current = scheduleConfig.value
        val newDays = if (current.daysOfWeek.contains(dayIso)) {
            current.daysOfWeek - dayIso
        } else {
            current.daysOfWeek + dayIso
        }
        updateSchedule(context, current.copy(daysOfWeek = newDays))
    }

    fun getNextTriggerText(): String {
        val app = getApplication<Application>()
        val config = scheduleConfig.value
        if (!config.enabled) return app.getString(R.string.schedule_disabled)
        if (config.daysOfWeek.isEmpty()) return app.getString(R.string.schedule_no_days)

        val nextTrigger = calculator.getNextTrigger(System.currentTimeMillis(), config)
            ?: return app.getString(R.string.schedule_none)
        val format = java.text.SimpleDateFormat("EEE h:mm a", java.util.Locale.getDefault())
        val actionStr = app.getString(
            if (nextTrigger.type == com.example.nightscreen.scheduling.ScheduleActionType.START) {
                R.string.schedule_action_on
            } else {
                R.string.schedule_action_off
            }
        )
        val timeStr = format.format(java.util.Date(nextTrigger.targetTimeMillis))
        return app.getString(R.string.schedule_next, actionStr, timeStr)
    }
}
