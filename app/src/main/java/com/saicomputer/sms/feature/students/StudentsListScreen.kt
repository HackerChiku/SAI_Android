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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(
    onOpenStudent: (String) -> Unit,
    onNewStudent: () -> Unit,
    viewModel: StudentsListViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val students = viewModel.students.collectAsLazyPagingItems()

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
            OutlinedTextField(
                value = filters.search,
                onValueChange = viewModel::onSearchChange,
                label = { Text("Search name, phone, ID") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                STATUS_OPTIONS.forEach { opt ->
                    FilterChip(
                        selected = filters.status == opt,
                        onClick = { viewModel.onStatusChange(opt) },
                        label = { Text(if (opt == "All") "All" else opt) }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SESSION_OPTIONS.forEach { opt ->
                    FilterChip(
                        selected = filters.registrationSession == opt,
                        onClick = { viewModel.onSessionChange(opt) },
                        label = { Text(SESSION_LABELS[opt] ?: opt) }
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

@Composable
private fun StudentRow(student: Student, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhotoAvatar(name = student.fullName)
            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, fontWeight = FontWeight.SemiBold)
                Text(
                    student.studentId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RegistrationSessionBadge(session = student.registrationSession)
            }
            StatusBadge(status = student.status)
        }
    }
}
