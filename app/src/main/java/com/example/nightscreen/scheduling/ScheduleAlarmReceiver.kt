package com.example.nightscreen.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives the AlarmManager trigger for the dimming schedule and delegates the
 * work to [ScheduleWorker] so it survives the receiver's short lifetime.
 */
class ScheduleAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ScheduleCoordinator.refreshSchedule(context)
    }
}
