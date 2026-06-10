package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.theme.appColors
import com.saicomputer.sms.core.ui.theme.appDimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun BackdateEntryCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    date: String?,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(
                appColors().warning.copy(alpha = 0.65f),
                appDimens().strokeDashed,
                appDimens().spacingMd
            )
            .background(appColors().warningContainer, appDimens().fieldShape)
            .padding(appDimens().spacing14),
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10)
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = appColors().warning,
                modifier = Modifier.size(appDimens().iconSizeXl)
            )
            Text(
                "Record as backdated entry",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = appColors().warning,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = appColors().warning,
                    uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        if (enabled) {
            BackdateDateField(
                value = date,
                onValueChange = onDateChange
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = appColors().warning,
                    modifier = Modifier.size(appDimens().iconSizeSm)
                )
                Text(
                    "No confirmation email will be sent automatically. Receipt PDF will still be generated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors().warning,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackdateDateField(
    value: String?,
    onValueChange: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = appColors().warning,
        unfocusedBorderColor = appColors().warning.copy(alpha = 0.7f),
        focusedContainerColor = appColors().warningContainer,
        unfocusedContainerColor = appColors().warningContainer,
        focusedTextColor = appColors().warning,
        unfocusedTextColor = appColors().warning
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatBackdateDisplay(value),
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text("dd/mm/yyyy", color = appColors().warning.copy(alpha = 0.5f))
            },
            trailingIcon = {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = appColors().warning)
            },
            shape = appDimens().fieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showPicker = true }
        )
    }

    if (showPicker) {
        val initialMillis = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = LocalDate.now(Formatters.IST)
                        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    return utcTimeMillis <= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(picked.toString())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun formatBackdateDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val d = LocalDate.parse(iso)
        String.format("%02d/%02d/%04d", d.dayOfMonth, d.monthValue, d.year)
    }.getOrDefault("")
}

private fun Modifier.dashedBorder(color: Color, strokeWidth: Dp, cornerRadius: Dp): Modifier =
    this.drawBehind {
        val strokeWidthPx = strokeWidth.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        val corner = cornerRadius.toPx()
        drawRoundRect(
            color = color,
            style = Stroke(width = strokeWidthPx, pathEffect = dash),
            cornerRadius = CornerRadius(corner, corner)
        )
    }
