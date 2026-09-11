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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import com.example.nightscreen.ui.components.ScreenContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nightscreen.R
import com.example.nightscreen.data.model.FilterPreset
import com.example.nightscreen.ui.theme.CornerRadius
import com.example.nightscreen.ui.theme.Dimens
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

    ScreenContainer {
        Text(
            text = stringResource(R.string.presets_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        // Presets Grid
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
            allPresets.chunked(2).forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
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
                            shape = CornerRadius.Card,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(Dimens.SpaceL)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(Dimens.SwatchLarge)
                                        .clip(CircleShape)
                                        .shadow(
                                            elevation = if (isSelected) 6.dp else 1.dp,
                                            shape = CircleShape,
                                            ambientColor = presetColor.copy(alpha = 0.35f),
                                            spotColor = presetColor.copy(alpha = 0.4f)
                                        )
                                        .background(presetColor)
                                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
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
                                        modifier = Modifier.size(Dimens.TouchTarget)
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

        if (prefs.customPresets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No custom presets yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Use the color picker below to create one",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Color Temperature (Kelvin) Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CornerRadius.Card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceXL),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.presets_kelvin_title, kelvinValue.toInt()),
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
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.SwatchMedium)
                            .clip(CircleShape)
                            .background(Color(kelvinHex or 0xFF000000L))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    )
                    Slider(
                        value = kelvinValue,
                        onValueChange = { kelvinValue = it },
                        valueRange = 1500f..4500f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
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
                        Text(stringResource(R.string.action_apply))
                    }
                }
            }
        }

        // Custom Color Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CornerRadius.Card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceXL),
                verticalArrangement = Arrangement.spacedBy(Dimens.ScreenGap)
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
                                .size(Dimens.SwatchSmall)
                                .clip(CircleShape)
                                .background(Color(hex or 0xFF000000L))
                                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
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
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(Color(currentColorHex or 0xFF000000L))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), MaterialTheme.shapes.large)
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

                // RGB Sliders (tinted to each channel)
                Text(stringResource(R.string.slider_red, red.toInt()))
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFE57373),
                        activeTrackColor = Color(0xFFE57373),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Text(stringResource(R.string.slider_green, green.toInt()))
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF81C784),
                        activeTrackColor = Color(0xFF81C784),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Text(stringResource(R.string.slider_blue, blue.toInt()))
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF64B5F6),
                        activeTrackColor = Color(0xFF64B5F6),
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

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
        var showError by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                showSaveDialog = false
                showError = false 
            },
            title = { Text(stringResource(R.string.save_preset_title)) },
            text = {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { 
                        customName = it
                        showError = false 
                    },
                    label = { Text(stringResource(R.string.preset_name_label)) },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Preset name cannot be empty") }
                    } else null
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            viewModel.saveCustomPreset(customName, currentColorHex)
                            customName = ""
                            showSaveDialog = false
                            showError = false
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showSaveDialog = false
                    showError = false 
                }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
