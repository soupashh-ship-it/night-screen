package com.example.nightscreen.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nightscreen.R
import com.example.nightscreen.ui.theme.HapticKind
import com.example.nightscreen.ui.theme.LocalHaptics
import com.example.nightscreen.ui.viewmodel.ScheduleViewModel
import java.util.Calendar

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {
    val context = LocalContext.current
    val config by viewModel.scheduleConfig.collectAsState()
    val haptics = LocalHaptics.current

    val dayNames = stringArrayResource(R.array.day_names)

    fun showTimePicker(initialHour: Int, initialMinute: Int, onTimeSelected: (Int, Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onTimeSelected(hour, minute) },
            initialHour,
            initialMinute,
            false
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.schedule_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Enable Switch Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.schedule_enable_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.schedule_enable_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.enabled,
                    onCheckedChange = { isChecked ->
                        haptics.perform(HapticKind.TOGGLE)
                        viewModel.updateSchedule(context, config.copy(enabled = isChecked))
                    }
                )
            }
        }

        // Status Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (config.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (config.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = viewModel.getNextTriggerText(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (config.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Selection Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.schedule_timer_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.schedule_start_time), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatTime(config.startHour, config.startMinute),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        enabled = config.enabled,
                        onClick = {
                            showTimePicker(config.startHour, config.startMinute) { h, m ->
                                viewModel.updateSchedule(context, config.copy(startHour = h, startMinute = m))
                            }
                        }
                    ) {
                        Text(stringResource(R.string.schedule_set_start))
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.schedule_end_time), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatTime(config.endHour, config.endMinute),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        enabled = config.enabled,
                        onClick = {
                            showTimePicker(config.endHour, config.endMinute) { h, m ->
                                viewModel.updateSchedule(context, config.copy(endHour = h, endMinute = m))
                            }
                        }
                    ) {
                        Text(stringResource(R.string.schedule_set_end))
                    }
                }
            }
        }

        // Days of Week Selection Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.schedule_repeat_days),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dayNames.forEachIndexed { index, day ->
                        val isoDay = index + 1
                        val isSelected = config.daysOfWeek.contains(isoDay)
                        val enabled = config.enabled

                        Surface(
                            selected = isSelected,
                            enabled = enabled,
                            onClick = {
                                haptics.perform(HapticKind.SELECT)
                                viewModel.toggleDay(context, isoDay)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isSelected && enabled -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when {
                                isSelected && enabled -> MaterialTheme.colorScheme.onPrimary
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                enabled -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(cal.time)
}
