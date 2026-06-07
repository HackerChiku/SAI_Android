package com.saicomputer.sms.feature.subscriptions

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.theme.StatusAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsListScreen(
    onBack: () -> Unit,
    onOpenEnrollment: (String) -> Unit,
    viewModel: SubscriptionsListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingOnly by viewModel.pendingOnly.collectAsStateWithLifecycle()

    Scaffold(topBar = { SmsTopBar(title = "Subscriptions", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = pendingOnly,
                    onClick = { viewModel.togglePendingOnly() },
                    label = { Text("Pending this month") }
                )
            }
            when (val s = state) {
                is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize())
                is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load, modifier = Modifier.fillMaxSize())
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState(title = "No subscriptions", modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(s.data) { sub ->
                                Card(Modifier.fillMaxWidth().clickable { onOpenEnrollment(sub.enrollmentId) }) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(sub.studentName, fontWeight = FontWeight.SemiBold)
                                        Text(sub.courseName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${Formatters.formatInr(sub.monthlyFee)}/mo", style = MaterialTheme.typography.bodyMedium)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            sub.paidThroughDate?.let { GenericBadge("Paid through ${Formatters.formatDateIst(it)}") }
                                            if (sub.isPendingCurrentMonth) GenericBadge("Pending", StatusAmber)
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
}
