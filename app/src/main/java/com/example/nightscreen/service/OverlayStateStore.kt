package com.example.nightscreen.service

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object OverlayStateStore {

    const val PREFS_NAME = "overlay_state_prefs"
    private var prefs: SharedPreferences? = null

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentIntensity = MutableStateFlow(0.30f)
    val currentIntensity: StateFlow<Float> = _currentIntensity.asStateFlow()

    private val _currentColor = MutableStateFlow(0xFF000000L)
    val currentColor: StateFlow<Long> = _currentColor.asStateFlow()

    /** True while the accessibility overlay is actually rendering the scrim. */
    private val _accessibilityDimmingActive = MutableStateFlow(false)
    val accessibilityDimmingActive: StateFlow<Boolean> = _accessibilityDimmingActive.asStateFlow()

    /** True when the overlay was auto-started by [BatteryReceiver] battery-saver logic. */
    @Volatile
    var autoStartedByBattery: Boolean = false

    @Synchronized
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _isPaused.value = prefs?.getBoolean("is_paused", false) ?: false
        }
    }

    fun setAccessibilityDimming(active: Boolean) {
        _accessibilityDimmingActive.value = active
    }

    fun updateState(active: Boolean, paused: Boolean = false, intensity: Float? = null, color: Long? = null) {
        _isActive.value = active
        _isPaused.value = paused
        if (intensity != null) {
            _currentIntensity.value = intensity.coerceIn(0.05f, 0.95f)
        }
        if (color != null) {
            _currentColor.value = color
        }
        prefs?.edit()?.putBoolean("is_paused", paused)?.apply()
    }
}
