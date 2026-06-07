package com.saicomputer.sms.feature.receipts

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.ResendEmailDialog
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.ReceiptListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsListScreen(
    onBack: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var resendFor by remember { mutableStateOf<ReceiptListItem?>(null) }

    Scaffold(topBar = { SmsTopBar(title = "Receipts", onBack = onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize().padding(padding))
            is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load, modifier = Modifier.fillMaxSize().padding(padding))
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyState(title = "No receipts", modifier = Modifier.fillMaxSize().padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.data) { r ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(r.studentName, fontWeight = FontWeight.SemiBold)
                                            Text(Formatters.formatDateIst(r.generatedAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        CurrencyText(r.amount, bold = true)
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        GenericBadge(r.emailStatus.name)
                                        TextButton(onClick = { resendFor = r }) { Text("Resend Email") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    resendFor?.let { r ->
        ResendEmailDialog(
            defaultEmail = r.studentEmail,
            onConfirm = { email ->
                viewModel.resend(r.receiptId, email) { snackbarController.show(scope, it) }
                resendFor = null
            },
            onDismiss = { resendFor = null }
        )
    }
}
