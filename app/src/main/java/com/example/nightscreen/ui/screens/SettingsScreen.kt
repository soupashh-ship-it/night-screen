package com.example.nightscreen.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nightscreen.R
import com.example.nightscreen.ui.components.ScreenContainer
import com.example.nightscreen.ui.components.SectionHeader
import com.example.nightscreen.ui.components.SettingRow
import com.example.nightscreen.ui.theme.Dimens
import com.example.nightscreen.ui.viewmodel.MainViewModel
import com.example.nightscreen.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val hasOverlayPermission by mainViewModel.hasOverlayPermission.collectAsState()
    val hasNotificationPermission by mainViewModel.hasNotificationPermission.collectAsState()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val dynamicColor by settingsViewModel.dynamicColor.collectAsState()
    val hapticsEnabled by settingsViewModel.hapticsEnabled.collectAsState()
    val reduceMotion by settingsViewModel.reduceMotion.collectAsState()

    val openOverlaySettings = {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        )
    }
    val openNotificationSettings = {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        )
    }

    ScreenContainer {
        // --- Permissions ---
        SectionHeader(
            title = stringResource(R.string.settings_permissions),
            subtitle = stringResource(R.string.settings_permissions_subtitle)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL)
            ) {
                SettingRow(
                    title = stringResource(R.string.permission_overlay_title),
                    subtitle = if (hasOverlayPermission) {
                        stringResource(R.string.permission_overlay_granted)
                    } else {
                        stringResource(R.string.permission_overlay_required)
                    },
                    trailing = {
                        PermissionStatus(
                            granted = hasOverlayPermission,
                            onClick = openOverlaySettings
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(
                    title = stringResource(R.string.permission_notif_title),
                    subtitle = if (hasNotificationPermission) {
                        stringResource(R.string.permission_notif_granted)
                    } else {
                        stringResource(R.string.permission_notif_required)
                    },
                    trailing = {
                        PermissionStatus(
                            granted = hasNotificationPermission,
                            onClick = openNotificationSettings
                        )
                    }
                )
            }
        }

        // --- Appearance ---
        SectionHeader(
            title = stringResource(R.string.settings_appearance),
            subtitle = stringResource(R.string.settings_appearance_subtitle)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL)
            ) {
                SettingRow(
                    title = stringResource(R.string.settings_theme),
                    subtitle = when (themeMode) {
                        "DARK" -> stringResource(R.string.theme_dark_sub)
                        "LIGHT" -> stringResource(R.string.theme_light_sub)
                        else -> stringResource(R.string.theme_system_sub)
                    },
                    trailing = {
                        ThemeModeSelector(
                            selected = themeMode,
                            onSelect = settingsViewModel::setThemeMode
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(
                    title = stringResource(R.string.settings_dynamic_color),
                    subtitle = stringResource(R.string.settings_dynamic_color_sub),
                    trailing = {
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = settingsViewModel::setDynamicColor
                        )
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingRow(
                    title = stringResource(R.string.settings_reduce_motion),
                    subtitle = stringResource(R.string.settings_reduce_motion_sub),
                    trailing = {
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = settingsViewModel::setReduceMotion
                        )
                    }
                )
            }
        }

        // --- Behaviour ---
        SectionHeader(title = stringResource(R.string.settings_behaviour))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceL)
            ) {
                SettingRow(
                    title = stringResource(R.string.settings_haptics),
                    subtitle = stringResource(R.string.settings_haptics_sub),
                    trailing = {
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = settingsViewModel::setHapticsEnabled
                        )
                    }
                )
            }
        }

        // --- About ---
        SectionHeader(title = stringResource(R.string.settings_about))
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceL),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.IconMedium)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_about_card_title),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.settings_about_card_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatus(granted: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.NightlightRound,
            contentDescription = if (granted) {
                stringResource(R.string.permission_granted_cd)
            } else {
                stringResource(R.string.permission_denied_cd)
            },
            tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeModeSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(
            "SYSTEM" to R.string.theme_system,
            "LIGHT" to R.string.theme_light,
            "DARK" to R.string.theme_dark
        ).forEach { (value, labelRes) ->
            val label = stringResource(labelRes)
            val isSelected = selected == value
            androidx.compose.material3.Surface(
                onClick = { onSelect(value) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
