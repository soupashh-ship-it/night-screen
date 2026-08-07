package com.example.nightscreen.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nightscreen.data.model.DimmerPreferences
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.service.OverlayService
import com.example.nightscreen.service.OverlayStateStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = UserPreferencesRepository(application)

    val preferences: StateFlow<DimmerPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DimmerPreferences())

    val isOverlayActive: StateFlow<Boolean> = OverlayStateStore.isActive
    val isOverlayPaused: StateFlow<Boolean> = OverlayStateStore.isPaused

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(true)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            _hasNotificationPermission.value = granted
        } else {
            _hasNotificationPermission.value = true
        }
    }

    fun toggleFilter(context: Context) {
        val active = isOverlayActive.value
        val action = if (active) OverlayService.ACTION_STOP else OverlayService.ACTION_START
        sendServiceAction(context, action)
    }

    fun togglePause(context: Context) {
        val paused = isOverlayPaused.value
        val action = if (paused) OverlayService.ACTION_RESUME else OverlayService.ACTION_PAUSE
        sendServiceAction(context, action)
    }

    fun setIntensity(context: Context, intensity: Float) {
        val clamped = intensity.coerceIn(0.05f, 0.95f)
        viewModelScope.launch {
            repository.updateIntensity(clamped)
            if (isOverlayActive.value && !isOverlayPaused.value) {
                val intent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_UPDATE
                    putExtra(OverlayService.EXTRA_INTENSITY, clamped)
                }
                startServiceSafely(context, intent)
            }
        }
    }

    fun previewIntensity(context: Context, intensity: Float) {
        if (!isOverlayActive.value || isOverlayPaused.value) return
        val clamped = intensity.coerceIn(0.05f, 0.95f)
        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_UPDATE
            putExtra(OverlayService.EXTRA_INTENSITY, clamped)
        }
        startServiceSafely(context, intent)
    }

    fun selectPreset(context: Context, preset: FilterPreset) {
        viewModelScope.launch {
            repository.updateColor(preset.colorHex, preset.id)
            if (isOverlayActive.value && !isOverlayPaused.value) {
                val intent = Intent(context, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_UPDATE
                    putExtra(OverlayService.EXTRA_COLOR_HEX, preset.colorHex)
                }
                startServiceSafely(context, intent)
            }
        }
    }

    fun saveCustomPreset(name: String, colorHex: Long) {
        val id = "custom_${System.currentTimeMillis()}"
        val preset = FilterPreset(id = id, name = name, colorHex = colorHex, isCustom = true)
        viewModelScope.launch {
            repository.addCustomPreset(preset)
        }
    }

    fun deleteCustomPreset(presetId: String) {
        viewModelScope.launch {
            repository.deleteCustomPreset(presetId)
        }
    }

    fun setSyncHardwareBrightness(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSyncHardwareBrightness(enabled)
        }
    }

    fun setAutoBatterySaver(enabled: Boolean, threshold: Int = 15) {
        viewModelScope.launch {
            repository.updateAutoBatterySaver(enabled, threshold)
        }
    }

    private fun sendServiceAction(context: Context, serviceAction: String) {
        val intent = Intent(context, OverlayService::class.java).apply {
            action = serviceAction
        }
        startServiceSafely(context, intent)
    }

    private fun startServiceSafely(context: Context, intent: Intent) {
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
