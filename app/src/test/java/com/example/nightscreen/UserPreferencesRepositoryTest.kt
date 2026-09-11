package com.example.nightscreen

import android.app.Application
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserPreferencesRepositoryTest {

    private lateinit var context: Application
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        repository = UserPreferencesRepository(context)
    }

    @Test
    fun `custom preset with null kelvin preserves null kelvin after serialization`() = runTest {
        val preset = FilterPreset(
            id = "custom_test_1",
            name = "My Custom Dim",
            colorHex = 0xFF123456L,
            isCustom = true,
            kelvin = null
        )

        repository.addCustomPreset(preset)
        val prefs = repository.preferencesFlow.first()
        val retrieved = prefs.customPresets.firstOrNull { it.id == "custom_test_1" }

        assertNotNull("Custom preset should be found", retrieved)
        assertEquals("My Custom Dim", retrieved?.name)
        assertEquals(0xFF123456L, retrieved?.colorHex)
        assertNull("Kelvin should remain null when not specified", retrieved?.kelvin)
        assertTrue(retrieved?.isCustom == true)
    }

    @Test
    fun `custom preset with kelvin preserves kelvin value`() = runTest {
        val preset = FilterPreset(
            id = "custom_test_2",
            name = "Warm 2400K",
            colorHex = 0xFFFF9900L,
            isCustom = true,
            kelvin = 2400
        )

        repository.addCustomPreset(preset)
        val prefs = repository.preferencesFlow.first()
        val retrieved = prefs.customPresets.firstOrNull { it.id == "custom_test_2" }

        assertNotNull(retrieved)
        assertEquals("Warm 2400K", retrieved?.name)
        assertEquals(2400, retrieved?.kelvin)
    }

    @Test
    fun `custom preset name sanitizes delimiter characters`() = runTest {
        val preset = FilterPreset(
            id = "custom_test_3",
            name = "Name|With;Delimiters",
            colorHex = 0xFF00FF00L,
            isCustom = true,
            kelvin = null
        )

        repository.addCustomPreset(preset)
        val prefs = repository.preferencesFlow.first()
        val retrieved = prefs.customPresets.firstOrNull { it.id == "custom_test_3" }

        assertNotNull(retrieved)
        assertEquals("NameWithDelimiters", retrieved?.name)
    }

    @Test
    fun `deleteCustomPreset removes the preset`() = runTest {
        val preset = FilterPreset(
            id = "custom_delete_me",
            name = "Delete Me",
            colorHex = 0xFF000000L,
            isCustom = true
        )

        repository.addCustomPreset(preset)
        var prefs = repository.preferencesFlow.first()
        assertTrue(prefs.customPresets.any { it.id == "custom_delete_me" })

        repository.deleteCustomPreset("custom_delete_me")
        prefs = repository.preferencesFlow.first()
        assertFalse(prefs.customPresets.any { it.id == "custom_delete_me" })
    }
}
