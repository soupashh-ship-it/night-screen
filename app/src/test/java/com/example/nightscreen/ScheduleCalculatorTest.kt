package com.example.nightscreen

import com.example.nightscreen.data.model.ScheduleConfig
import com.example.nightscreen.scheduling.ScheduleActionType
import com.example.nightscreen.scheduling.ScheduleCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class ScheduleCalculatorTest {

    private lateinit var calculator: ScheduleCalculator

    @Before
    fun setUp() {
        calculator = ScheduleCalculator()
    }

    @Test
    fun `isScheduleActiveAt returns false when schedule is disabled`() {
        val schedule = ScheduleConfig(enabled = false, startHour = 22, endHour = 7)
        val active = calculator.isScheduleActiveAt(23, 0, 1, schedule)
        assertFalse(active)
    }

    @Test
    fun `isScheduleActiveAt handles standard same day schedule`() {
        // 08:00 to 17:00 on weekdays (Mon..Fri = 1..5)
        val schedule = ScheduleConfig(
            enabled = true,
            startHour = 8,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = setOf(1, 2, 3, 4, 5)
        )

        // Monday 10:00 AM -> Should be active
        assertTrue(calculator.isScheduleActiveAt(10, 0, 1, schedule))

        // Monday 07:30 AM -> Inactive
        assertFalse(calculator.isScheduleActiveAt(7, 30, 1, schedule))

        // Monday 17:30 PM -> Inactive
        assertFalse(calculator.isScheduleActiveAt(17, 30, 1, schedule))

        // Saturday (6) 10:00 AM -> Inactive (day not in schedule)
        assertFalse(calculator.isScheduleActiveAt(10, 0, 6, schedule))
    }

    @Test
    fun `isScheduleActiveAt handles midnight crossing schedule`() {
        // 22:00 to 07:00 next day on Monday (1)
        val schedule = ScheduleConfig(
            enabled = true,
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = setOf(1) // Monday only
        )

        // Monday 23:00 -> Active (Evening portion of Monday)
        assertTrue(calculator.isScheduleActiveAt(23, 0, 1, schedule))

        // Tuesday 02:00 -> Active (Morning portion of Monday's schedule, previousDay = Monday)
        assertTrue(calculator.isScheduleActiveAt(2, 0, 2, schedule))

        // Tuesday 08:00 -> Inactive (Past 07:00 end)
        assertFalse(calculator.isScheduleActiveAt(8, 0, 2, schedule))

        // Monday 02:00 -> Inactive (Morning of Monday belongs to Sunday's schedule, which isn't enabled)
        assertFalse(calculator.isScheduleActiveAt(2, 0, 1, schedule))
    }

    @Test
    fun `getNextTrigger calculates upcoming start and stop times`() {
        val schedule = ScheduleConfig(
            enabled = true,
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7)
        )

        // Set fixed time: 14:00 PM
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nextTrigger = calculator.getNextTrigger(cal.timeInMillis, schedule)
        assertNotNull(nextTrigger)
        assertEquals(ScheduleActionType.START, nextTrigger!!.type)

        val triggerCal = Calendar.getInstance().apply {
            timeInMillis = nextTrigger.targetTimeMillis
        }
        assertEquals(22, triggerCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal.get(Calendar.MINUTE))
    }
}
