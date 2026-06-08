package com.saicomputer.sms.feature.students

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.ListItemCard
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.PhotoAvatar
import com.saicomputer.sms.core.ui.RegistrationSessionBadge
import com.saicomputer.sms.core.ui.StatusBadge
import com.saicomputer.sms.data.model.Student

private val STATUS_OPTIONS = listOf("All", "Active", "PaymentPending", "Completed", "New", "Dropout", "NotTakenAdmission")
private val SESSION_OPTIONS = listOf("All", "Before2017", "After2017", "NewRecord")
private val SESSION_LABELS = mapOf(
    "All" to "All", "Before2017" to "Before 2017", "After2017" to "After 2017", "NewRecord" to "New Record"
)
private val STATUS_LABELS = mapOf(
    "All" to "All",
    "Active" to "Active",
    "PaymentPending" to "Payment Pending",
    "Completed" to "Completed",
    "New" to "New",
    "Dropout" to "Dropout",
    "NotTakenAdmission" to "Not Taken Admission"
)

private fun StudentFilters.hasActiveFilters(): Boolean =
    status != "All" || registrationSession != "All"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(
    onOpenStudent: (String) -> Unit,
    onNewStudent: () -> Unit,
    viewModel: StudentsListViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val students = viewModel.students.collectAsLazyPagingItems()
    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState
        ) {
            StudentFiltersSheet(
                filters = filters,
                onStatusChange = viewModel::onStatusChange,
                onSessionChange = viewModel::onSessionChange,
                onClear = {
                    viewModel.onStatusChange("All")
                    viewModel.onSessionChange("All")
                    showFilters = false
                },
                onDone = { showFilters = false }
            )
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNewStudent) {
                Icon(Icons.Outlined.Add, contentDescription = "New student")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filters.search,
                    onValueChange = viewModel::onSearchChange,
                    label = { Text("Search name, phone, ID") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showFilters = true }) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = "Filters",
                        tint = if (filters.hasActiveFilters()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFiltersSheet(
    filters: StudentFilters,
    onStatusChange: (String) -> Unit,
    onSessionChange: (String) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Text(
            "Status",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            STATUS_OPTIONS.forEach { opt ->
                FilterChip(
                    selected = filters.status == opt,
                    onClick = { onStatusChange(opt) },
                    label = { Text(STATUS_LABELS[opt] ?: opt) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Registration era",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SESSION_OPTIONS.forEach { opt ->
                FilterChip(
                    selected = filters.registrationSession == opt,
                    onClick = { onSessionChange(opt) },
                    label = { Text(SESSION_LABELS[opt] ?: opt) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                Text("Clear")
            }
            Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun StudentRow(student: Student, onClick: () -> Unit) {
    ListItemCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PhotoAvatar(name = student.fullName, size = 52)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = student.status, fontSize = 9.sp)
                RegistrationSessionBadge(session = student.registrationSession, fontSize = 9.sp)
            }
        }
    }
}
