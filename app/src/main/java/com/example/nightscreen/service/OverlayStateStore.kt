package com.example.nightscreen.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object OverlayStateStore {

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentIntensity = MutableStateFlow(0.30f)
    val currentIntensity: StateFlow<Float> = _currentIntensity.asStateFlow()

    private val _currentColor = MutableStateFlow(0xFF000000L)
    val currentColor: StateFlow<Long> = _currentColor.asStateFlow()

    fun updateState(active: Boolean, paused: Boolean = false, intensity: Float? = null, color: Long? = null) {
        _isActive.value = active
        _isPaused.value = paused
        if (intensity != null) {
            _currentIntensity.value = intensity.coerceIn(0.05f, 0.95f)
        }
        if (color != null) {
            _currentColor.value = color
        }
    }
}
