package com.example.nightscreen.scheduling

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Front door for schedule automation. Every schedule event (boot, time change,
 * schedule edit, alarm firing) runs [ScheduleWorker] via WorkManager so the
 * work outlives the broadcast receiver that requested it.
 */
object ScheduleCoordinator {

    private const val SCHEDULE_WORK_NAME = "schedule-alarm"

    /** Re-evaluates the schedule now and (re)arms the next alarm. */
    fun refreshSchedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<ScheduleWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            SCHEDULE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
