package com.saicomputer.sms.feature.students

import com.saicomputer.sms.core.ui.studentStatusColor
import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.ShimmerPagingRow
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.ProfileMenuButton
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens

private val STATUS_OPTIONS = listOf("All", "Active", "PaymentPending", "Completed", "New", "Dropout", "NotTakenAdmission")
private val SESSION_OPTIONS = listOf("All", "Before2017", "After2017", "NewRecord")
private val SESSION_LABELS = mapOf(
    "All" to "All Registration",
    "Before2017" to "Before 2017",
    "After2017" to "After 2017",
    "NewRecord" to "New Record"
)
private val STATUS_LABELS = mapOf(
    "All" to "All Status",
    "Active" to "Active",
    "PaymentPending" to "Payment Pending",
    "Completed" to "Completed",
    "New" to "New",
    "Dropout" to "Dropout",
    "NotTakenAdmission" to "Not Taken Admission"
)

private val STATUS_LIST_LABELS = mapOf(
    StudentStatus.New to "New",
    StudentStatus.Active to "Active",
    StudentStatus.PaymentPending to "Pmt Pending",
    StudentStatus.Completed to "Completed",
    StudentStatus.Dropout to "Dropout",
    StudentStatus.NotTakenAdmission to "Not Admitted"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(
    user: User? = null,
    onOpenStudent: (String) -> Unit,
    onNewStudent: () -> Unit,
    viewModel: StudentsListViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val students = viewModel.students.collectAsLazyPagingItems()
    val searchFocus = remember { FocusRequester() }
    var showFilterDialog by remember { mutableStateOf(false) }
    val hasActiveFilters = filters.status != "All" || filters.registrationSession != "All"

    Column(modifier = Modifier.fillMaxSize()) {
        StudentsListHeader(
            user = user,
            onSearchClick = { searchFocus.requestFocus() },
            onNewStudent = onNewStudent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = appDimens().spacingLg)
        ) {
            Spacer(Modifier.height(appDimens().spacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filters.search,
                    onValueChange = viewModel::onSearchChange,
                    placeholder = { Text("Search by name, phone, or ID") },
                    singleLine = true,
                    shape = appDimens().fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocus)
                )
                Box {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        modifier = Modifier
                            .size(appDimens().iconSizeListBox)
                            .clip(appDimens().fieldShape)
                            .background(if (hasActiveFilters) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = "Filters",
                            tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (hasActiveFilters) {
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

            if (showFilterDialog) {
                StudentsFilterDialog(
                    status = filters.status,
                    registrationSession = filters.registrationSession,
                    onStatusChange = viewModel::onStatusChange,
                    onSessionChange = viewModel::onSessionChange,
                    onClear = {
                        viewModel.onStatusChange("All")
                        viewModel.onSessionChange("All")
                    },
                    onDismiss = { showFilterDialog = false }
                )
            }

            Spacer(Modifier.height(appDimens().spacingMd))

            val refreshState = students.loadState.refresh
            when {
                refreshState is LoadState.Loading && students.itemCount == 0 ->
                    LoadingSkeleton(modifier = Modifier.fillMaxSize())

                refreshState is LoadState.Error && students.itemCount == 0 ->
                    ErrorState(
                        message = (refreshState.error.message ?: "Failed to load"),
                        onRetry = { students.retry() },
                        modifier = Modifier.fillMaxSize()
                    )

                students.itemCount == 0 -> {
                    val filtered = filters.search.isNotBlank() ||
                        filters.status != "All" || filters.registrationSession != "All"
                    if (filtered) {
                        EmptyState(
                            title = "No students match your filters",
                            actionLabel = "Clear filters",
                            onAction = viewModel::clearFilters,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        EmptyState(
                            title = "No students yet",
                            actionLabel = "New Student",
                            onAction = onNewStudent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = appDimens().spacingLg),
                        verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
                    ) {
                        items(
                            count = students.itemCount,
                            key = students.itemKey { it.studentId }
                        ) { index ->
                            val student = students[index]
                            if (student != null) {
                                StudentRow(student = student, onClick = { onOpenStudent(student.studentId) })
                            }
                        }
                        if (students.loadState.append is LoadState.Loading) {
                            item {
                                ShimmerPagingRow()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentsListHeader(
    user: User?,
    onSearchClick: () -> Unit,
    onNewStudent: () -> Unit
) {
    AppTopBarBox {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().spacingLg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Students",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(appDimens().iconSizeXxl)
                        .clip(CircleShape)
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(appDimens().iconSizeXxl)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable(onClick = onNewStudent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "New student",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
                ProfileMenuButton(user = user)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentsFilterDialog(
    status: String,
    registrationSession: String,
    onStatusChange: (String) -> Unit,
    onSessionChange: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = appDimens().statShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(appDimens().iconSizeMd),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)
            ) {
                Text(
                    "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                FilterDropdown(
                    displayLabel = STATUS_LABELS[status] ?: "All Status",
                    options = STATUS_OPTIONS,
                    optionLabels = STATUS_LABELS,
                    selected = status,
                    onSelected = onStatusChange,
                    modifier = Modifier.fillMaxWidth()
                )
                FilterDropdown(
                    displayLabel = SESSION_LABELS[registrationSession] ?: "All Registration",
                    options = SESSION_OPTIONS,
                    optionLabels = SESSION_LABELS,
                    selected = registrationSession,
                    onSelected = onSessionChange,
                    modifier = Modifier.fillMaxWidth()
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
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
                Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            shape = appDimens().fieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
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
private fun StudentRow(student: Student, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(appDimens().spacing14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacing14)
        ) {
            ColoredPhotoAvatar(name = student.fullName, size = 48)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
            ) {
                Text(
                    student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    student.studentId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
            ) {
                val statusColor = studentStatusColor(student.status)
                Pill(
                    text = STATUS_LIST_LABELS[student.status] ?: student.status.name,
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium
                )
                when (student.registrationSession) {
                    RegistrationSession.Before2017 ->
                        Pill("Before 2017", appColors().purple, style = MaterialTheme.typography.labelMedium)
                    RegistrationSession.After2017 ->
                        Pill("After 2017", appColors().info, style = MaterialTheme.typography.labelMedium)
                    RegistrationSession.NewRecord -> Unit
                }
            }
        }
    }
}
