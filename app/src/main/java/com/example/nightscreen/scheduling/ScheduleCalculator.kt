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
     * Resolves the active ScheduleConfig, substituting start/end hours with calculated solar times
     * if useSunsetSunrise is enabled.
     */
    fun resolveEffectiveSchedule(
        schedule: ScheduleConfig,
        cal: Calendar = Calendar.getInstance()
    ): ScheduleConfig {
        if (!schedule.useSunsetSunrise) return schedule
        val solar = SunsetSunriseCalculator.calculateSolarTimes(cal, schedule.latitude, schedule.longitude)
        return schedule.copy(
            startHour = solar.sunsetHour,
            startMinute = solar.sunsetMinute,
            endHour = solar.sunriseHour,
            endMinute = solar.sunriseMinute
        )
    }

    /**
     * Determines whether the filter schedule should be active at the given hour, minute, and day of week.
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

        val effSchedule = resolveEffectiveSchedule(schedule)
        val currentMinutesOfDay = hour * 60 + minute
        val startMinutesOfDay = effSchedule.startHour * 60 + effSchedule.startMinute
        val endMinutesOfDay = effSchedule.endHour * 60 + effSchedule.endMinute

        return if (startMinutesOfDay < endMinutesOfDay) {
            effSchedule.daysOfWeek.contains(dayOfWeek) &&
                    currentMinutesOfDay >= startMinutesOfDay &&
                    currentMinutesOfDay < endMinutesOfDay
        } else if (startMinutesOfDay > endMinutesOfDay) {
            if (currentMinutesOfDay >= startMinutesOfDay) {
                effSchedule.daysOfWeek.contains(dayOfWeek)
            } else if (currentMinutesOfDay < endMinutesOfDay) {
                val previousDay = if (dayOfWeek == 1) 7 else dayOfWeek - 1
                effSchedule.daysOfWeek.contains(previousDay)
            } else {
                false
            }
        } else {
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

        for (dayOffset in 0..7) {
            val candidateCal = (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val candidateIsoDay = getIsoDayOfWeek(candidateCal)
            val candidateEffSchedule = resolveEffectiveSchedule(schedule, candidateCal)

            if (!isActiveNow) {
                if (candidateEffSchedule.daysOfWeek.contains(candidateIsoDay)) {
                    candidateCal.set(Calendar.HOUR_OF_DAY, candidateEffSchedule.startHour)
                    candidateCal.set(Calendar.MINUTE, candidateEffSchedule.startMinute)
                    candidateCal.set(Calendar.SECOND, 0)
                    candidateCal.set(Calendar.MILLISECOND, 0)

                    if (candidateCal.timeInMillis > nowMillis) {
                        return ScheduleAction(ScheduleActionType.START, candidateCal.timeInMillis)
                    }
                }
            } else {
                candidateCal.set(Calendar.HOUR_OF_DAY, candidateEffSchedule.endHour)
                candidateCal.set(Calendar.MINUTE, candidateEffSchedule.endMinute)
                candidateCal.set(Calendar.SECOND, 0)
                candidateCal.set(Calendar.MILLISECOND, 0)

                if (candidateCal.timeInMillis > nowMillis) {
                    return ScheduleAction(ScheduleActionType.STOP, candidateCal.timeInMillis)
                }
            }
        }

        return null
    }

    fun getIsoDayOfWeek(calendar: Calendar): Int {
        val calDay = calendar.get(Calendar.DAY_OF_WEEK)
        return if (calDay == Calendar.SUNDAY) 7 else calDay - 1
    }
}
