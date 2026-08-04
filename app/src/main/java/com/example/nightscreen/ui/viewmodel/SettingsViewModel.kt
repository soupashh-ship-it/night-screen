package com.example.nightscreen.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nightscreen.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    val themeMode: StateFlow<String> = repository.preferencesFlow
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val dynamicColor: StateFlow<Boolean> = repository.preferencesFlow
        .map { it.dynamicColor }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hapticsEnabled: StateFlow<Boolean> = repository.preferencesFlow
        .map { it.hapticsEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reduceMotion: StateFlow<Boolean> = repository.preferencesFlow
        .map { it.reduceMotion }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDynamicColor(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticsEnabled(enabled)
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReduceMotion(enabled)
        }
    }
}
