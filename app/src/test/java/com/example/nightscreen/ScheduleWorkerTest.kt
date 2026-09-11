package com.example.nightscreen

import android.app.Application
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.nightscreen.data.model.ScheduleConfig
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.scheduling.ScheduleWorker
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScheduleWorkerTest {

    private lateinit var context: Application

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        com.example.nightscreen.service.OverlayStateStore.updateState(active = false, paused = false)
    }

    @Test
    fun `disabled schedule succeeds without starting the overlay service`() = runTest {
        // Default persisted prefs: schedule disabled.
        val worker = TestListenableWorkerBuilder<ScheduleWorker>(context).build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertNull("No service should start when the schedule is disabled", shadowOf(application()).nextStartedService)
    }

    @Test
    fun `enabled schedule inside an active window starts the overlay service`() = runTest {
        val repository = UserPreferencesRepository(context)
        val now = Calendar.getInstance()

        val start = (now.clone() as Calendar).apply { add(Calendar.MINUTE, -2) }
        val end = (now.clone() as Calendar).apply { add(Calendar.MINUTE, 2) }
        repository.updateSchedule(
            ScheduleConfig(
                enabled = true,
                startHour = start.get(Calendar.HOUR_OF_DAY),
                startMinute = start.get(Calendar.MINUTE),
                endHour = end.get(Calendar.HOUR_OF_DAY),
                endMinute = end.get(Calendar.MINUTE),
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7)
            )
        )
        ShadowSettings.setCanDrawOverlays(true)

        val worker = TestListenableWorkerBuilder<ScheduleWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        val startedService = shadowOf(application()).nextStartedService
        assertNotNull("Schedule-active worker should start the overlay service", startedService)
        assertEquals(
            "com.example.nightscreen.service.OverlayService",
            startedService?.component?.className
        )
    }

    @Test
    fun `enabled schedule outside active window stops the overlay service when active`() = runTest {
        val repository = UserPreferencesRepository(context)
        val now = Calendar.getInstance()

        val start = (now.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -4) }
        val end = (now.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -2) }
        repository.updateSchedule(
            ScheduleConfig(
                enabled = true,
                startHour = start.get(Calendar.HOUR_OF_DAY),
                startMinute = start.get(Calendar.MINUTE),
                endHour = end.get(Calendar.HOUR_OF_DAY),
                endMinute = end.get(Calendar.MINUTE),
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7)
            )
        )
        ShadowSettings.setCanDrawOverlays(true)

        com.example.nightscreen.service.OverlayStateStore.updateState(active = true, paused = false)

        val worker = TestListenableWorkerBuilder<ScheduleWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        val stoppedService = shadowOf(application()).nextStoppedService
        assertNotNull("Schedule-inactive worker should stop the running overlay service", stoppedService)
        assertEquals(
            "com.example.nightscreen.service.OverlayService",
            stoppedService?.component?.className
        )
    }

    private fun application(): Application = context
}
