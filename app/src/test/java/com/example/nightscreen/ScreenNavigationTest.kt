package com.example.nightscreen

import com.example.nightscreen.ui.navigation.Screen
import org.junit.Assert.*
import org.junit.Test

class ScreenNavigationTest {

    @Test
    fun `bottomNavItems are non null and have valid routes`() {
        val items = Screen.bottomNavItems
        assertEquals(5, items.size)
        items.forEach { screen ->
            assertNotNull("Screen should not be null", screen)
            assertNotNull("Route should not be null", screen.route)
            assertTrue("Route should not be empty", screen.route.isNotBlank())
        }
    }
}
