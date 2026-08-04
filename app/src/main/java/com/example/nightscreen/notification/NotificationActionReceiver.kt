package com.example.nightscreen.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nightscreen.data.repository.UserPreferencesRepository
import com.example.nightscreen.service.OverlayService
import com.example.nightscreen.service.OverlayStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = UserPreferencesRepository(context)
                val currentPrefs = repository.preferencesFlow.first()

                when (action) {
                    NotificationFactory.ACTION_TOGGLE_PAUSE -> {
                        val isPaused = OverlayStateStore.isPaused.value
                        val serviceAction = if (isPaused) OverlayService.ACTION_RESUME else OverlayService.ACTION_PAUSE
                        startServiceWithAction(context, serviceAction)
                    }

                    NotificationFactory.ACTION_DECREASE_INTENSITY -> {
                        val newIntensity = (currentPrefs.intensity - 0.10f).coerceIn(0.05f, 0.95f)
                        repository.updateIntensity(newIntensity)
                        startServiceWithAction(context, OverlayService.ACTION_UPDATE)
                    }

                    NotificationFactory.ACTION_INCREASE_INTENSITY -> {
                        val newIntensity = (currentPrefs.intensity + 0.10f).coerceIn(0.05f, 0.95f)
                        repository.updateIntensity(newIntensity)
                        startServiceWithAction(context, OverlayService.ACTION_UPDATE)
                    }

                    NotificationFactory.ACTION_STOP_SERVICE -> {
                        startServiceWithAction(context, OverlayService.ACTION_STOP)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun startServiceWithAction(context: Context, action: String) {
        val serviceIntent = Intent(context, OverlayService::class.java).apply {
            this.action = action
        }
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
