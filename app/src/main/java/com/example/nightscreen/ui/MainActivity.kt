package com.example.nightscreen.ui

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.nightscreen.R
import com.example.nightscreen.ui.navigation.AppNavigation
import com.example.nightscreen.ui.navigation.Screen
import com.example.nightscreen.ui.theme.LocalReduceMotion
import com.example.nightscreen.ui.theme.NightscreenTheme
import com.example.nightscreen.ui.theme.ProvideHaptics
import com.example.nightscreen.ui.viewmodel.MainViewModel
import com.example.nightscreen.ui.viewmodel.ScheduleViewModel
import com.example.nightscreen.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupLauncherShortcuts()
        handleShortcutIntent(intent)

        val navigateTo = intent?.getStringExtra("navigate_to")
        val startDestination = if (navigateTo == "settings") Screen.Settings.route else Screen.Main.route

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState()
            val hapticsEnabled by settingsViewModel.hapticsEnabled.collectAsState()
            val reduceMotion by settingsViewModel.reduceMotion.collectAsState()

            NightscreenTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {
                ProvideHaptics(enabled = hapticsEnabled) {
                    CompositionLocalProvider(
                        LocalReduceMotion provides reduceMotion
                    ) {
                        AppNavigation(
                            mainViewModel = mainViewModel,
                            scheduleViewModel = scheduleViewModel,
                            settingsViewModel = settingsViewModel,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        if (intent?.getStringExtra("action") == "toggle") {
            mainViewModel.toggleFilter(this)
        }
    }

    private fun setupLauncherShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return
            val toggleShortcut = ShortcutInfo.Builder(this, "toggle_night_screen")
                .setShortLabel("Toggle Filter")
                .setLongLabel("Toggle Night Screen Filter")
                .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                .setIntent(Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("action", "toggle")
                })
                .build()

            try {
                shortcutManager.dynamicShortcuts = listOf(toggleShortcut)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.checkPermissions()
    }
}
