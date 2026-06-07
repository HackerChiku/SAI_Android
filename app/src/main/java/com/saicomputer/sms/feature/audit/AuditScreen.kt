package com.saicomputer.sms.feature.audit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    onBack: () -> Unit,
    viewModel: AuditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { SmsTopBar(title = "Audit Log", onBack = onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize().padding(padding))
            is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load, modifier = Modifier.fillMaxSize().padding(padding))
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyState(title = "No audit entries", modifier = Modifier.fillMaxSize().padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.data) { entry ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(entry.action, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${entry.userId} • ${Formatters.formatDateIst(entry.timestamp, withTime = true)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    entry.target?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
