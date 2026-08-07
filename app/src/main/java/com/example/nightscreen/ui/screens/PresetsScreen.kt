package com.example.nightscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nightscreen.R
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.ui.theme.HapticKind
import com.example.nightscreen.ui.theme.LocalHaptics
import com.example.nightscreen.ui.viewmodel.MainViewModel

@Composable
fun PresetsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsState()
    val haptics = LocalHaptics.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }

    var red by remember { mutableFloatStateOf(0f) }
    var green by remember { mutableFloatStateOf(0f) }
    var blue by remember { mutableFloatStateOf(0f) }

    var kelvinValue by remember { mutableFloatStateOf(2700f) }

    val currentColorHex = remember(red, green, blue) {
        val r = red.toInt().coerceIn(0, 255)
        val g = green.toInt().coerceIn(0, 255)
        val b = blue.toInt().coerceIn(0, 255)
        (0xFFL shl 24) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
    }

    val allPresets = remember(prefs.customPresets) {
        FilterPreset.DEFAULT_PRESETS + prefs.customPresets
    }

    val quickColors = remember {
        listOf(
            0xFFD32F2FL, // Deep Red
            0xFFFFB454L, // Warm Amber
            0xFFFF8A3DL, // Soft Orange
            0xFFFFC107L, // Golden Yellow
            0xFF1E3A8AL, // Night Blue
            0xFF4A148CL  // Deep Violet
        )
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
            text = stringResource(R.string.presets_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Presets Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            allPresets.chunked(2).forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPresets.forEach { preset ->
                        val isSelected = prefs.selectedPresetId == preset.id
                        val presetColor = Color(preset.colorHex or 0xFF000000L)

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .selectable(
                                    selected = isSelected,
                                    role = androidx.compose.ui.semantics.Role.RadioButton,
                                    onClick = {
                                        haptics.perform(HapticKind.SELECT)
                                        viewModel.selectPreset(context, preset)
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(presetColor)
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = stringResource(R.string.preset_selected_cd),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (preset.isCustom) {
                                    IconButton(
                                        onClick = { viewModel.deleteCustomPreset(preset.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.preset_delete_cd),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (rowPresets.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Color Temperature (Kelvin) Selector Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Color Temperature (${kelvinValue.toInt()}K)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val kelvinHex = remember(kelvinValue) {
                    FilterPreset.kelvinToColorHex(kelvinValue.toInt())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(kelvinHex or 0xFF000000L))
                            .border(1.5.dp, Color.Gray, CircleShape)
                    )
                    Slider(
                        value = kelvinValue,
                        onValueChange = { kelvinValue = it },
                        valueRange = 1500f..4500f,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val tempPreset = FilterPreset(
                                id = "kelvin_${kelvinValue.toInt()}",
                                name = "Warm ${kelvinValue.toInt()}K",
                                colorHex = kelvinHex,
                                kelvin = kelvinValue.toInt()
                            )
                            viewModel.selectPreset(context, tempPreset)
                        }
                    ) {
                        Text("Apply")
                    }
                }
            }
        }

        // Custom Color Selector Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.custom_color_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Quick Color Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    quickColors.forEach { hex ->
                        val r = ((hex ushr 16) and 0xFFL).toFloat()
                        val g = ((hex ushr 8) and 0xFFL).toFloat()
                        val b = (hex and 0xFFL).toFloat()
                        val hexLabel = String.format("#%06X", hex and 0xFFFFFFL)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(hex or 0xFF000000L))
                                .border(1.dp, Color.Gray, CircleShape)
                                .semantics { contentDescription = hexLabel }
                                .clickable {
                                    red = r
                                    green = g
                                    blue = b
                                }
                        )
                    }
                }

                // Color Preview
                val hexDigits = String.format("%06X", currentColorHex and 0xFFFFFFL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentColorHex or 0xFF000000L))
                            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .semantics { contentDescription = hexDigits }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(
                                R.string.rgb_values,
                                red.toInt(),
                                green.toInt(),
                                blue.toInt()
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.hex_value, hexDigits),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val customPreset = FilterPreset(
                                id = "custom_${System.currentTimeMillis()}",
                                name = context.getString(R.string.custom_preset_name, hexDigits),
                                colorHex = currentColorHex,
                                isCustom = true
                            )
                            viewModel.selectPreset(context, customPreset)
                        }
                    ) {
                        Text(stringResource(R.string.action_apply))
                    }
                }

                // RGB Sliders
                Text(stringResource(R.string.slider_red, red.toInt()))
                Slider(value = red, onValueChange = { red = it }, valueRange = 0f..255f)

                Text(stringResource(R.string.slider_green, green.toInt()))
                Slider(value = green, onValueChange = { green = it }, valueRange = 0f..255f)

                Text(stringResource(R.string.slider_blue, blue.toInt()))
                Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..255f)

                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.save_preset))
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.save_preset_title)) },
            text = {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text(stringResource(R.string.preset_name_label)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            viewModel.saveCustomPreset(customName, currentColorHex)
                            customName = ""
                            showSaveDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
