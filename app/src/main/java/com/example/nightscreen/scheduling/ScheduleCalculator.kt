package com.example.nightscreen.scheduling

import com.example.nightscreen.data.model.ScheduleConfig
import java.util.Calendar

enum class ScheduleActionType { START, STOP }

data class ScheduleAction(
    val type: ScheduleActionType,
    val targetTimeMillis: Long
)

class ScheduleCalculator {

    /**
     * Determines whether the filter schedule should be active at the given hour, minute, and day of week.
     * @param hour 0-23
     * @param minute 0-59
     * @param dayOfWeek 1=Mon, 2=Tue, ..., 7=Sun
     * @param schedule ScheduleConfig containing start/end time and active days of week
     */
    fun isScheduleActiveAt(
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        schedule: ScheduleConfig
    ): Boolean {
        if (!schedule.enabled || schedule.daysOfWeek.isEmpty()) {
            return false
        }

        val currentMinutesOfDay = hour * 60 + minute
        val startMinutesOfDay = schedule.startHour * 60 + schedule.startMinute
        val endMinutesOfDay = schedule.endHour * 60 + schedule.endMinute

        return if (startMinutesOfDay < endMinutesOfDay) {
            // Same day schedule (e.g., 08:00 to 17:00)
            schedule.daysOfWeek.contains(dayOfWeek) &&
                    currentMinutesOfDay >= startMinutesOfDay &&
                    currentMinutesOfDay < endMinutesOfDay
        } else if (startMinutesOfDay > endMinutesOfDay) {
            // Midnight crossing schedule (e.g., 22:00 to 07:00)
            if (currentMinutesOfDay >= startMinutesOfDay) {
                // Evening portion: check today's day of week
                schedule.daysOfWeek.contains(dayOfWeek)
            } else if (currentMinutesOfDay < endMinutesOfDay) {
                // Morning portion: check yesterday's day of week
                val previousDay = if (dayOfWeek == 1) 7 else dayOfWeek - 1
                schedule.daysOfWeek.contains(previousDay)
            } else {
                false
            }
        } else {
            // Start == End time -> disabled
            false
        }
    }

    /**
     * Calculates the next upcoming trigger action (START or STOP) and its epoch millis.
     */
    fun getNextTrigger(
        nowMillis: Long,
        schedule: ScheduleConfig
    ): ScheduleAction? {
        if (!schedule.enabled || schedule.daysOfWeek.isEmpty()) {
            return null
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
        }

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentDay = getIsoDayOfWeek(calendar)

        val isActiveNow = isScheduleActiveAt(currentHour, currentMinute, currentDay, schedule)

        // Find closest trigger time within next 7 days
        for (dayOffset in 0..7) {
            val candidateCal = (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val candidateIsoDay = getIsoDayOfWeek(candidateCal)

            if (!isActiveNow) {
                // Looking for next START
                if (schedule.daysOfWeek.contains(candidateIsoDay)) {
                    candidateCal.set(Calendar.HOUR_OF_DAY, schedule.startHour)
                    candidateCal.set(Calendar.MINUTE, schedule.startMinute)
                    candidateCal.set(Calendar.SECOND, 0)
                    candidateCal.set(Calendar.MILLISECOND, 0)

                    if (candidateCal.timeInMillis > nowMillis) {
                        return ScheduleAction(ScheduleActionType.START, candidateCal.timeInMillis)
                    }
                }
            } else {
                // Looking for next STOP
                candidateCal.set(Calendar.HOUR_OF_DAY, schedule.endHour)
                candidateCal.set(Calendar.MINUTE, schedule.endMinute)
                candidateCal.set(Calendar.SECOND, 0)
                candidateCal.set(Calendar.MILLISECOND, 0)

                if (candidateCal.timeInMillis > nowMillis) {
                    return ScheduleAction(ScheduleActionType.STOP, candidateCal.timeInMillis)
                }
            }
        }

        return null
    }

    /**
     * Converts Java Calendar.DAY_OF_WEEK (Sun=1, Mon=2..Sat=7) to ISO-8601 Day of Week (Mon=1..Sun=7).
     */
    fun getIsoDayOfWeek(calendar: Calendar): Int {
        val calDay = calendar.get(Calendar.DAY_OF_WEEK)
        return if (calDay == Calendar.SUNDAY) 7 else calDay - 1
    }
}
