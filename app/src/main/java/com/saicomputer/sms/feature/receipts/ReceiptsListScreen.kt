package com.saicomputer.sms.feature.receipts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.ListItemCard
import com.saicomputer.sms.core.ui.ListItemIconBox
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.ResendEmailDialog
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.emailStatusColor
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.ReceiptListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsListScreen(
    snackbarController: SnackbarController,
    viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var resendFor by remember { mutableStateOf<ReceiptListItem?>(null) }

    when (val s = state) {
        is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize())
        is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load, modifier = Modifier.fillMaxSize())
        is UiState.Success -> {
            if (s.data.isEmpty()) {
                EmptyState(title = "No receipts", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(s.data) { r ->
                        ReceiptRow(receipt = r, onResend = { resendFor = r })
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReceiptRow(receipt: ReceiptListItem, onResend: () -> Unit) {
    ListItemCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            ListItemIconBox(icon = Icons.AutoMirrored.Outlined.ReceiptLong)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    receipt.studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    Formatters.formatDateIst(receipt.generatedAt, withTime = true),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    receipt.receiptId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GenericBadge(receipt.emailStatus.name, emailStatusColor(receipt.emailStatus))
                    if (receipt.voidedWithPayment) {
                        GenericBadge("Voided", StatusRed)
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyText(
                    receipt.amount,
                    style = MaterialTheme.typography.titleSmall,
                    bold = true
                )
                OutlinedButton(onClick = onResend) {
                    Text("Resend Email", fontSize = 12.sp)
                }
            }
        }
    }
}
