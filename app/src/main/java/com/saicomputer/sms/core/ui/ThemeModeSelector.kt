package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.saicomputer.sms.core.theme.ThemeMode
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun ThemeModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
    ) {
        ProfileSectionHeader(
            icon = Icons.Outlined.Palette,
            title = "Appearance",
            subtitle = "Choose light, dark, or match your device setting."
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
        ) {
            ThemeMode.entries.forEach { mode ->
                ThemeModeOption(
                    mode = mode,
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconBackground = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }
    val iconTint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(appDimens().fieldShape)
            .border(appDimens().strokeHairline, borderColor, appDimens().fieldShape)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = appDimens().spacingMd, horizontal = appDimens().spacingSm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
    ) {
        Box(
            modifier = Modifier
                .size(appDimens().iconSizeListBox)
                .clip(appDimens().iconShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = themeModeIcon(mode),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(appDimens().iconSizeLg)
            )
        }
        Text(
            text = mode.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun themeModeIcon(mode: ThemeMode): ImageVector = when (mode) {
    ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
    ThemeMode.LIGHT -> Icons.Outlined.Brightness6
    ThemeMode.DARK -> Icons.Outlined.Brightness4
}
