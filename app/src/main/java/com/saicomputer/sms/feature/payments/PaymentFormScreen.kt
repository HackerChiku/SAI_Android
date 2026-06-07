package com.saicomputer.sms.feature.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.AmountField
import com.saicomputer.sms.core.ui.BackdateToggle
import com.saicomputer.sms.core.ui.DatePickerField
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.PaymentMethod
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormScreen(
    enrollmentId: String,
    onBack: () -> Unit,
    onRecorded: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: PaymentFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(enrollmentId) { viewModel.initialize(enrollmentId) }

    val msg: (String) -> Unit = { snackbarController.show(scope, it) }

    Scaffold(topBar = { SmsTopBar(title = "Record Payment", onBack = onBack) }) { padding ->
        if (state.loading) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.padding(16.dp))
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.enrollment?.let { e ->
                Text(e.courseName ?: e.courseId, style = MaterialTheme.typography.titleMedium)
            }

            if (!state.isSubscription) {
                Text("Select installment", style = MaterialTheme.typography.titleMedium)
                if (state.installments.isEmpty()) {
                    Text("No unpaid installments.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.installments.forEach { inst ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.selectInstallment(inst) }
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.selectedInstallmentId == inst.installmentId,
                                onClick = { viewModel.selectInstallment(inst) }
                            )
                            Column(Modifier.weight(1f)) {
                                Text("#${inst.installmentNumber} — due ${Formatters.formatDateIst(inst.dueDate)}")
                                Text(
                                    "Remaining ${Formatters.formatInr(inst.amountDue - inst.amountPaid)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } else {
                DatePickerField(
                    value = "${state.billingMonth}-01".takeIf { state.billingMonth.isNotBlank() },
                    onValueChange = { d -> viewModel.update { it.copy(billingMonth = d.substring(0, 7)) } },
                    label = "Billing month"
                )
                state.enrollment?.let { e ->
                    if (e.packageType == PackageType.PACKAGE_3_1) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Use 3+1 package (4th month bonus)")
                            Switch(checked = state.usePackage, onCheckedChange = { on -> viewModel.update { it.copy(usePackage = on) } })
                        }
                    }
                }
            }

            AmountField(state.amount, { v -> viewModel.update { it.copy(amount = v) } }, "Amount", Modifier.fillMaxWidth())

            Text("Payment method", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { m ->
                    FilterChip(
                        selected = state.method == m,
                        onClick = { viewModel.update { it.copy(method = m) } },
                        label = { Text(m.name) }
                    )
                }
            }
            if (state.method == PaymentMethod.UPI) {
                OutlinedTextField(
                    value = state.upiRef,
                    onValueChange = { v -> viewModel.update { it.copy(upiRef = v) } },
                    label = { Text("UPI transaction reference *") },
                    isError = state.upiRefError != null,
                    supportingText = state.upiRefError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DatePickerField(state.paymentDate, { d -> viewModel.update { it.copy(paymentDate = d) } }, "Payment date", disableFuture = true)

            OutlinedTextField(
                value = state.notes,
                onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            if (can(user, "system.backdate")) {
                BackdateToggle(
                    enabled = state.backdateEnabled,
                    onEnabledChange = { v -> viewModel.update { it.copy(backdateEnabled = v) } },
                    date = state.effectiveCreatedAt,
                    onDateChange = { d -> viewModel.update { it.copy(effectiveCreatedAt = d) } }
                )
            }

            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)

            Button(
                onClick = { viewModel.submit(onSuccess = onRecorded, onMessage = msg) },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.submitting) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Record Payment")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
