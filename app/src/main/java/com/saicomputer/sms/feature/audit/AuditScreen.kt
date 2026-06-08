package com.saicomputer.sms.feature.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineLight
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.data.model.AuditLogEntry
import com.saicomputer.sms.data.model.User
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)

@Composable
fun AuditScreen(
    user: User? = null,
    viewModel: AuditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val actionOptions by viewModel.actionOptions.collectAsStateWithLifecycle()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OutlineLight,
        unfocusedBorderColor = OutlineVariantLight,
        focusedContainerColor = BaseWhite,
        unfocusedContainerColor = BaseWhite,
        focusedPlaceholderColor = OnSurfaceVariantLightColor,
        unfocusedPlaceholderColor = OnSurfaceVariantLightColor
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AuditListHeader(user = user)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionFilterDropdown(
                    selected = filters.action,
                    options = actionOptions,
                    onSelected = viewModel::onActionChange,
                    fieldColors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
                AuditDateFilter(
                    date = filters.date,
                    onDateChange = viewModel::onDateChange,
                    onClear = viewModel::clearDateFilter,
                    fieldColors = fieldColors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                is UiState.Loading -> LoadingSkeleton(modifier = Modifier.fillMaxSize())
                is UiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = viewModel::load,
                    modifier = Modifier.fillMaxSize()
                )
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState(
                            title = "No audit entries",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = BaseWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                itemsIndexed(s.data, key = { _, entry -> entry.logId }) { index, entry ->
                                    AuditLogRow(entry = entry)
                                    if (index < s.data.lastIndex) {
                                        HorizontalDivider(
                                            color = OutlineVariantLight,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditListHeader(user: User?) {
    val initial = user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Audit Log",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandBlue.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, color = BaseWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionFilterDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val display = if (selected == "All") "All Actions" else humanizeAuditActionForFilter(selected)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = OnSurfaceVariantLightColor)
            },
            shape = FieldShape,
            colors = fieldColors,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(if (option == "All") "All Actions" else humanizeAuditActionForFilter(option))
                    },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditDateFilter(
    date: String?,
    onDateChange: (String?) -> Unit,
    onClear: () -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = formatAuditDateDisplay(date),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("dd / mm / yyyy") },
            trailingIcon = {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = OnSurfaceVariantLightColor)
            },
            shape = FieldShape,
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
                    onClear()
                    showPicker = false
                }) { Text("Clear") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun AuditLogRow(entry: AuditLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pill(text = entry.actionLabel, color = StatusBlue, fontSize = 10.sp)
            Text(
                Formatters.formatDateIst(entry.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariantLightColor
            )
        }

        Text(
            entry.subjectLine,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            entry.performerLine,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantLightColor
        )

        entry.detailLine?.let { detail ->
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantLightColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatAuditDateDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val date = LocalDate.parse(iso)
        String.format("%02d / %02d / %04d", date.dayOfMonth, date.monthValue, date.year)
    }.getOrElse { Formatters.formatDateIst(iso) }
}

private fun humanizeAuditActionForFilter(action: String): String {
    if (action == "All") return "All Actions"
    return AuditLogEntry(
        logId = "",
        action = action
    ).actionLabel
}
