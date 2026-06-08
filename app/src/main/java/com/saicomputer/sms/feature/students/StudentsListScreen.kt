package com.saicomputer.sms.feature.students

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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineLight
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.core.ui.theme.StatusPurple
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.core.ui.theme.StatusZinc
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import com.saicomputer.sms.data.model.User

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

private val STATUS_LIST_COLORS = mapOf(
    StudentStatus.New to StatusBlue,
    StudentStatus.Active to StatusEmerald,
    StudentStatus.PaymentPending to StatusAmber,
    StudentStatus.Completed to StatusGray,
    StudentStatus.Dropout to StatusRed,
    StudentStatus.NotTakenAdmission to StatusZinc
)

private val FieldShape = RoundedCornerShape(12.dp)
private val CardShape = RoundedCornerShape(14.dp)

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

    Column(modifier = Modifier.fillMaxSize()) {
        StudentsListHeader(
            user = user,
            onSearchClick = { searchFocus.requestFocus() },
            onNewStudent = onNewStudent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = filters.search,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Search by name, phone, or ID") },
                singleLine = true,
                shape = FieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BaseWhite,
                    unfocusedContainerColor = BaseWhite,
                    focusedBorderColor = OutlineLight,
                    unfocusedBorderColor = OutlineVariantLight
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocus)
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterDropdown(
                    displayLabel = STATUS_LABELS[filters.status] ?: "All Status",
                    options = STATUS_OPTIONS,
                    optionLabels = STATUS_LABELS,
                    selected = filters.status,
                    onSelected = viewModel::onStatusChange,
                    modifier = Modifier.weight(1f)
                )
                FilterDropdown(
                    displayLabel = SESSION_LABELS[filters.registrationSession] ?: "All Registration",
                    options = SESSION_OPTIONS,
                    optionLabels = SESSION_LABELS,
                    selected = filters.registrationSession,
                    onSelected = viewModel::onSessionChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

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
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
private fun StudentsListHeader(
    user: User?,
    onSearchClick: () -> Unit,
    onNewStudent: () -> Unit
) {
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
            "Students",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = BaseWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BrandRed)
                    .clickable(onClick = onNewStudent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "New student",
                    tint = BaseWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
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
                Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = OnSurfaceVariantLightColor)
            },
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BaseWhite,
                unfocusedContainerColor = BaseWhite,
                focusedBorderColor = OutlineLight,
                unfocusedBorderColor = OutlineVariantLight,
                disabledContainerColor = BaseWhite
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ColoredPhotoAvatar(name = student.fullName, size = 48)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    color = OnSurfaceVariantLightColor
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val statusColor = STATUS_LIST_COLORS[student.status] ?: StatusGray
                Pill(
                    text = STATUS_LIST_LABELS[student.status] ?: student.status.name,
                    color = statusColor,
                    fontSize = 10.sp
                )
                when (student.registrationSession) {
                    RegistrationSession.Before2017 ->
                        Pill("Before 2017", StatusPurple, fontSize = 10.sp)
                    RegistrationSession.After2017 ->
                        Pill("After 2017", StatusBlue, fontSize = 10.sp)
                    RegistrationSession.NewRecord -> Unit
                }
            }
        }
    }
}
