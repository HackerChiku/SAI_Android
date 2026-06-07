package com.saicomputer.sms.feature.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesListScreen(
    onBack: () -> Unit,
    onNewCourse: () -> Unit,
    onEditCourse: (String) -> Unit,
    viewModel: CoursesListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val canCreate = can(user, "courses.create")

    Scaffold(
        topBar = { SmsTopBar(title = "Courses", onBack = onBack) },
        floatingActionButton = {
            if (canCreate) {
                FloatingActionButton(onClick = onNewCourse) {
                    Icon(Icons.Outlined.Add, contentDescription = "New course")
                }
            }
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize().padding(padding))
            is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load, modifier = Modifier.fillMaxSize().padding(padding))
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(s.data) { course ->
                        CourseRow(course, onClick = { onEditCourse(course.courseId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: Course, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(course.courseName, fontWeight = FontWeight.SemiBold)
            Text(course.courseFullName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fee ${Formatters.formatInr(course.fee)}", style = MaterialTheme.typography.bodyMedium)
                Text("+ ${Formatters.formatInr(course.enrollmentFee)} enroll", style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GenericBadge(course.billingType.name)
                if (course.billingType == BillingType.Subscription) {
                    GenericBadge("${Formatters.formatInr(course.monthlyFee)}/mo")
                } else {
                    GenericBadge("Max ${course.maxInstallments} inst.")
                }
                GenericBadge(if (course.hasTopics) "${course.topicsCount ?: 0} topics" else "No topics")
            }
        }
    }
}
