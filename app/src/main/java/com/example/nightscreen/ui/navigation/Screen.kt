package com.example.nightscreen.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Main : Screen("main", "Dimmer", Icons.Default.NightlightRound)
    object Presets : Screen("presets", "Presets", Icons.Default.ColorLens)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.Schedule)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Main, Presets, Schedule, Settings, About)
    }
}
