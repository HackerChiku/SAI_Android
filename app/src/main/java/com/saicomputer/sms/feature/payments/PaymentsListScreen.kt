package com.saicomputer.sms.feature.payments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.format.REGISTRATION_SESSION_LABELS
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.PaymentListItem
import com.saicomputer.sms.data.model.PaymentMethod
import com.saicomputer.sms.data.model.PaymentStatus
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.User

private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun PaymentsListScreen(
    user: User? = null,
    snackbarController: SnackbarController,
    viewModel: PaymentsListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val canVoid = can(user, "payments.void")
    val canEditBilling = can(user, "payments.editBillingMonth")

    var voidTarget by remember { mutableStateOf<PaymentListItem?>(null) }
    var editBillingTarget by remember { mutableStateOf<PaymentListItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        PaymentsListHeader(user = user)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(horizontal = 16.dp)
        ) {
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
                            title = "No payments recorded yet",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.data, key = { it.payment.paymentId }) { item ->
                                PaymentHistoryCard(
                                    item = item,
                                    canVoid = canVoid,
                                    canEditBilling = canEditBilling,
                                    onViewReceipt = {
                                        val receiptId = item.payment.receiptId
                                        if (receiptId.isNullOrBlank()) {
                                            snackbarController.show(scope, "No receipt for this payment")
                                        } else {
                                            viewModel.loadReceipt(
                                                receiptId,
                                                onSuccess = { receipt ->
                                                    val url = receipt.previewUrl ?: receipt.downloadUrl
                                                    if (url != null) {
                                                        context.startActivity(
                                                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                        )
                                                    } else {
                                                        snackbarController.show(
                                                            scope,
                                                            "Receipt ${receipt.receiptId}"
                                                        )
                                                    }
                                                },
                                                onError = { snackbarController.show(scope, it) }
                                            )
                                        }
                                    },
                                    onVoid = { voidTarget = item },
                                    onEditBillingMonth = { editBillingTarget = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    voidTarget?.let {
        VoidPaymentDialog(
            onConfirm = { reason ->
                voidTarget = null
                viewModel.voidPayment(
                    paymentId = it.payment.paymentId,
                    reason = reason,
                    onSuccess = { snackbarController.show(scope, "Payment voided") },
                    onError = { snackbarController.show(scope, it) }
                )
            },
            onDismiss = { voidTarget = null }
        )
    }

    editBillingTarget?.let { item ->
        EditBillingMonthDialog(
            currentMonth = item.payment.billingMonth.orEmpty(),
            onConfirm = { month ->
                editBillingTarget = null
                viewModel.editBillingMonth(
                    paymentId = item.payment.paymentId,
                    newBillingMonth = month,
                    onSuccess = { snackbarController.show(scope, "Billing month updated") },
                    onError = { snackbarController.show(scope, it) }
                )
            },
            onDismiss = { editBillingTarget = null }
        )
    }
}

@Composable
private fun PaymentsListHeader(user: User?) {
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
            "Payment History",
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

@Composable
private fun PaymentHistoryCard(
    item: PaymentListItem,
    canVoid: Boolean,
    canEditBilling: Boolean,
    onViewReceipt: () -> Unit,
    onVoid: () -> Unit,
    onEditBillingMonth: () -> Unit
) {
    val payment = item.payment
    val isVoided = payment.status == PaymentStatus.Voided
    val methodLabel = when (payment.paymentMethod) {
        PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
        else -> payment.paymentMethod.name
    }
    val showSessionBadge = item.registrationSession != null &&
        item.registrationSession != RegistrationSession.NewRecord
    val billingMonth = payment.billingMonth?.takeIf { it.isNotBlank() }
    val showEditMonth = canEditBilling && !isVoided && billingMonth != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredPhotoAvatar(name = item.studentName, size = 44)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.studentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (showSessionBadge) {
                            Pill(
                                text = REGISTRATION_SESSION_LABELS[item.registrationSession] ?: "",
                                color = StatusBlue,
                                fontSize = 9.sp
                            )
                        }
                    }
                    if (item.courseName.isNotBlank()) {
                        Text(
                            item.courseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariantLightColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Pill(
                    text = if (isVoided) "Voided" else "Paid",
                    color = if (isVoided) StatusRed else StatusEmerald,
                    fontSize = 10.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurrencyText(
                    payment.amount,
                    style = MaterialTheme.typography.headlineSmall,
                    bold = true
                )
                if (!payment.receiptId.isNullOrBlank()) {
                    TextButton(onClick = onViewReceipt) {
                        Text("View Receipt", color = BrandBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${Formatters.formatDateIst(payment.paymentDate)} · $methodLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariantLightColor
                    )
                    billingMonth?.let {
                        Text(
                            "Billing: ${Formatters.formatBillingMonth(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariantLightColor
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (canVoid && !isVoided) {
                        TextButton(onClick = onVoid) {
                            Text("Void", color = BrandRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (showEditMonth) {
                        TextButton(onClick = onEditBillingMonth) {
                            Text("Edit Month", color = BrandBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoidPaymentDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Void payment") },
        text = {
            Column {
                Text("This cannot be undone. Enter a reason:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Reason") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason.trim()) },
                enabled = reason.isNotBlank()
            ) { Text("Void", color = BrandRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditBillingMonthDialog(
    currentMonth: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var month by remember(currentMonth) { mutableStateOf(currentMonth) }
    val valid = month.matches(Regex("""\d{4}-\d{2}"""))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit billing month") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter the billing month as YYYY-MM.")
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2026-03") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(month.trim()) },
                enabled = valid
            ) { Text("Save", color = BrandBlue) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
