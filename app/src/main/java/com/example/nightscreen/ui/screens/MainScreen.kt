package com.example.nightscreen.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nightscreen.R
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.ui.components.NoticeBanner
import com.example.nightscreen.ui.components.PresetChip
import com.example.nightscreen.ui.components.ScreenContainer
import com.example.nightscreen.ui.components.StatusPill
import com.example.nightscreen.ui.theme.CornerRadius
import com.example.nightscreen.ui.theme.Dimens
import com.example.nightscreen.ui.theme.HapticKind
import com.example.nightscreen.ui.theme.LocalHaptics
import com.example.nightscreen.ui.theme.LocalReduceMotion
import com.example.nightscreen.ui.viewmodel.MainViewModel
import com.example.nightscreen.ui.viewmodel.ScheduleViewModel
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    scheduleViewModel: ScheduleViewModel? = null
) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsState()
    val isActive by viewModel.isOverlayActive.collectAsState()
    val isPaused by viewModel.isOverlayPaused.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()

    val haptics = LocalHaptics.current
    val reduceMotion = LocalReduceMotion.current

    // Local slider state so the drag is not fighting DataStore round-trips.
    // Synced whenever the committed preference changes externally (presets…).
    var sliderValue by remember { mutableFloatStateOf(prefs.intensity) }
    LaunchedEffect(prefs.intensity) { sliderValue = prefs.intensity }

    val openOverlaySettings = {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        )
    }

    // Throttle live overlay previews during a drag; commit once on drag end.
    val previewThrottle = remember { PreviewThrottle() }

    // --- Motion: the dimmer control breathes softly while active, and the
    // toggle pops on state change. Both collapse to static under reduced motion.
    val infinite = rememberInfiniteTransition(label = "dimmerGlow")
    val breathe by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBreathe"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive && !isPaused && !reduceMotion) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "glowAlpha"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (reduceMotion) 1f else if (isActive && !isPaused) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 320f),
        label = "toggleScale"
    )

    val statusText = when {
        !hasOverlayPermission -> stringResource(R.string.status_permission_needed)
        isActive && isPaused -> stringResource(R.string.status_paused)
        isActive -> stringResource(R.string.status_active)
        else -> stringResource(R.string.status_off)
    }
    val hintText = when {
        !hasOverlayPermission -> stringResource(R.string.permission_hint)
        isActive && isPaused -> stringResource(R.string.paused_hint)
        isActive -> stringResource(R.string.active_hint)
        else -> stringResource(R.string.activate_hint)
    }

    ScreenContainer {
        // --- Compact top bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                text = statusText,
                containerColor = when {
                    !hasOverlayPermission -> MaterialTheme.colorScheme.errorContainer
                    isActive && !isPaused -> MaterialTheme.colorScheme.primaryContainer
                    isActive -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = when {
                    !hasOverlayPermission -> MaterialTheme.colorScheme.onErrorContainer
                    isActive && !isPaused -> MaterialTheme.colorScheme.onPrimaryContainer
                    isActive -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // --- Contextual permission notices ---
        if (!hasOverlayPermission) {
            NoticeBanner(
                title = stringResource(R.string.overlay_banner_title),
                body = stringResource(R.string.overlay_banner_body),
                buttonText = stringResource(R.string.action_grant),
                onButtonClick = openOverlaySettings
            )
        } else if (!hasNotificationPermission) {
            NoticeBanner(
                title = stringResource(R.string.notification_banner_title),
                body = stringResource(R.string.notification_banner_body),
                buttonText = stringResource(R.string.action_enable),
                onButtonClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                icon = Icons.Default.NightlightRound
            )
        }

        // --- Dominant dimmer control ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CornerRadius.Card,
            colors = CardDefaults.cardColors(
                containerColor = if (isActive && !isPaused) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
            ) {
                // Intensity readout: monospace + tabular figures for a precise,
                // instrument-like feel.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.intensity_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${(sliderValue * 100).roundToInt()}%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-3).sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Activation control with soft amber glow while active.
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(184.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.42f * glowAlpha * if (reduceMotion) 1f else breathe
                                        ),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    IconButton(
                        onClick = {
                            if (!hasOverlayPermission) {
                                openOverlaySettings()
                            } else {
                                haptics.perform(HapticKind.ACTIVATE)
                                viewModel.toggleFilter(context)
                            }
                        },
                        modifier = Modifier
                            .size(Dimens.ToggleButton)
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .clip(CircleShape)
                            .background(
                                color = when {
                                    !hasOverlayPermission -> MaterialTheme.colorScheme.surfaceContainerHighest
                                    isActive && !isPaused -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.18f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = stringResource(R.string.toggle_filter),
                            modifier = Modifier.size(52.dp),
                            tint = when {
                                !hasOverlayPermission -> MaterialTheme.colorScheme.onSurfaceVariant
                                isActive && !isPaused -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }

                // Hint line under the toggle.
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Pause / Stop actions while running
                if (isActive) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
                        OutlinedButton(onClick = {
                            haptics.perform(HapticKind.TOGGLE)
                            viewModel.togglePause(context)
                        }) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSmall)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isPaused) stringResource(R.string.action_resume)
                                else stringResource(R.string.action_pause)
                            )
                        }
                        Button(
                            onClick = { viewModel.toggleFilter(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(stringResource(R.string.action_stop))
                        }
                    }
                }

                // --- Intensity slider with +/- steps (thumb zone) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val next = (sliderValue - 0.05f).coerceAtLeast(0.05f)
                            sliderValue = next
                            viewModel.setIntensity(context, next)
                        },
                        modifier = Modifier
                            .size(Dimens.SliderStepButton)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = stringResource(R.string.decrease_intensity),
                            modifier = Modifier.size(Dimens.IconMedium)
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            sliderValue = newValue
                            if (previewThrottle.shouldSend(SystemClock.uptimeMillis())) {
                                viewModel.previewIntensity(context, newValue)
                            }
                        },
                        onValueChangeFinished = {
                            viewModel.setIntensity(context, sliderValue)
                        },
                        valueRange = 0.05f..0.95f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val next = (sliderValue + 0.05f).coerceAtMost(0.95f)
                            sliderValue = next
                            viewModel.setIntensity(context, next)
                        },
                        modifier = Modifier
                            .size(Dimens.SliderStepButton)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.increase_intensity),
                            modifier = Modifier.size(Dimens.IconMedium)
                        )
                    }
                }
            }
        }

        // --- Compact preset selector (custom presets first so they surface) ---
        val quickPresets = remember(prefs.customPresets) {
            (prefs.customPresets + FilterPreset.DEFAULT_PRESETS).take(5)
        }
        if (quickPresets.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                Text(
                    text = stringResource(R.string.presets_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
                ) {
                    items(quickPresets, key = { it.id }) { preset ->
                        PresetChip(
                            name = preset.name,
                            color = Color(preset.colorHex or 0xFF000000L),
                            selected = prefs.selectedPresetId == preset.id,
                            onClick = {
                                haptics.perform(HapticKind.SELECT)
                                viewModel.selectPreset(context, preset)
                            }
                        )
                    }
                }
            }
        }

        // --- Schedule summary ---
        val nextTriggerText = scheduleViewModel?.getNextTriggerText()
        if (nextTriggerText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CornerRadius.Card,
                colors = CardDefaults.cardColors(
                    containerColor = if (prefs.schedule.enabled) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpaceL),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                ) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = if (prefs.schedule.enabled) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(Dimens.IconMedium)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.schedule_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = nextTriggerText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (prefs.schedule.enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
