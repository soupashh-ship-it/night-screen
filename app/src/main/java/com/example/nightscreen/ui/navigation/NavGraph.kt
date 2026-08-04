package com.example.nightscreen.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nightscreen.ui.screens.*
import com.example.nightscreen.ui.viewmodel.MainViewModel
import com.example.nightscreen.ui.viewmodel.ScheduleViewModel
import com.example.nightscreen.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel,
    scheduleViewModel: ScheduleViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    viewModel = mainViewModel,
                    scheduleViewModel = scheduleViewModel
                )
            }
            composable(Screen.Presets.route) {
                PresetsScreen(viewModel = mainViewModel)
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(viewModel = scheduleViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    mainViewModel = mainViewModel
                )
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
