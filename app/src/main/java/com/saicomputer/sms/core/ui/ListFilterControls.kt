package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.Dialog
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.theme.appDimens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

val listFilterFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        disabledContainerColor = MaterialTheme.colorScheme.surface
    )

@Composable
fun ListSearchFilterSortBar(
    search: String,
    onSearchChange: (String) -> Unit,
    searchPlaceholder: String,
    hasActiveFilters: Boolean,
    hasActiveSort: Boolean,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            placeholder = { Text(searchPlaceholder) },
            singleLine = true,
            shape = appDimens().fieldShape,
            colors = listFilterFieldColors,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions.Default,
            modifier = Modifier.weight(1f)
        )
        ListToolbarIconButton(
            icon = Icons.Outlined.FilterList,
            contentDescription = "Filters",
            active = hasActiveFilters,
            onClick = onFilterClick
        )
        ListToolbarIconButton(
            icon = Icons.Outlined.SwapVert,
            contentDescription = "Sort",
            active = hasActiveSort,
            onClick = onSortClick
        )
    }
}

@Composable
private fun ListToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(appDimens().iconSizeListBox)
                .clip(appDimens().fieldShape)
                .background(
                    if (active) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(appDimens().spacing10)
                    .size(appDimens().spacingSm)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    displayLabel: String,
    options: List<String>,
    optionLabels: Map<String, String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Outlined.UnfoldMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = appDimens().fieldShape,
            colors = listFilterFieldColors,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabels[option] ?: option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun FilterDialogShell(
    title: String,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            shape = appDimens().statShape,
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(appDimens().iconSizeMd),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10)
                ) {
                    TextButton(
                        onClick = onClear,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = appDimens().fieldShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text("Apply", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SortDialog(
    title: String,
    options: List<String>,
    optionLabels: Map<String, String>,
    selected: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    FilterDialogShell(
        title = title,
        onClear = { onSelected(options.first()) },
        onDismiss = onDismiss
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(appDimens().fieldShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onSelected(option) }
                    .padding(horizontal = appDimens().spacingMd, vertical = appDimens().spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    optionLabels[option] ?: option,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDateFilter(
    label: String,
    date: String?,
    onDateChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = formatListDateDisplay(date),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("dd / mm / yyyy") },
            trailingIcon = {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = appDimens().fieldShape,
            colors = listFilterFieldColors,
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
        val initialMillis = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
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
                        onDateChange(picked.toString())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateChange(null)
                    showPicker = false
                }) { Text("Clear") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun formatListDateDisplay(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return runCatching { LocalDate.parse(isoDate) }.getOrNull()?.let { date ->
        String.format("%02d / %02d / %04d", date.dayOfMonth, date.monthValue, date.year)
    } ?: isoDate
}
