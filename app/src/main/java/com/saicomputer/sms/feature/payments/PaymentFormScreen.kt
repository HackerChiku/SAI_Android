package com.saicomputer.sms.feature.payments

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.BackdateEntryCard
import com.saicomputer.sms.core.ui.TitleBarBackButton
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.FormLoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.data.model.InstallmentStatus
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.PaymentMethod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.saicomputer.sms.core.ui.theme.appDimens


private val FORM_PAYMENT_METHODS = listOf(
    PaymentMethod.CASH,
    PaymentMethod.UPI,
    PaymentMethod.QR
)

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
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PaymentFormHeader(onBack = onBack)

        if (state.loading) {
            FormLoadingSkeleton(Modifier.fillMaxSize())
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
        ) {
            state.enrollment?.let { enrollment ->
                StudentPaymentSummaryCard(enrollment = enrollment)
            }

            if (can(user, "system.backdate")) {
                BackdateEntryCard(
                    enabled = state.backdateEnabled,
                    onEnabledChange = { v -> viewModel.update { it.copy(backdateEnabled = v) } },
                    date = state.effectiveCreatedAt,
                    onDateChange = { d -> viewModel.update { it.copy(effectiveCreatedAt = d) } }
                )
            }

            if (!state.isSubscription) {
                InstallmentPickerCard(
                    installments = state.allInstallments,
                    selectedId = state.selectedInstallmentId,
                    onSelect = viewModel::selectInstallment
                )
            } else {
                SubscriptionBillingCard(
                    billingMonth = state.billingMonth,
                    usePackage = state.usePackage,
                    showPackageToggle = state.enrollment?.packageType == PackageType.PACKAGE_3_1,
                    onBillingMonthChange = { m -> viewModel.update { it.copy(billingMonth = m) } },
                    onUsePackageChange = { on -> viewModel.update { it.copy(usePackage = on) } },
                    fieldColors = fieldColors
                )
            }

            PaymentDetailsCard(
                amount = state.amount,
                onAmountChange = { v -> viewModel.update { it.copy(amount = v) } },
                method = state.method,
                onMethodChange = { m -> viewModel.update { it.copy(method = m) } },
                upiRef = state.upiRef,
                onUpiRefChange = { v -> viewModel.update { it.copy(upiRef = v) } },
                paymentDate = state.paymentDate,
                onPaymentDateChange = { d -> viewModel.update { it.copy(paymentDate = d) } },
                notes = state.notes,
                onNotesChange = { v -> viewModel.update { it.copy(notes = v) } },
                fieldColors = fieldColors
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(appDimens().spacingMd))
            Button(
                onClick = { viewModel.submit(onSuccess = onRecorded, onMessage = msg) },
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = appDimens().spacingMd),
                shape = appDimens().fieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                )
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(appDimens().iconSizeListInner),
                        strokeWidth = appDimens().spacingXxs,
                        color = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Text("Record Payment", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = appDimens().spacingXs))
                }
            }
        }
    }
}

@Composable
private fun PaymentFormHeader(onBack: () -> Unit) {
    AppTopBarBox {
        AppTitleBarRow(
            leading = {
                TitleBarBackButton(onBack = onBack)
                Text(
                    "Record Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}

@Composable
private fun StudentPaymentSummaryCard(enrollment: Enrollment) {
    val billingLabel = when (enrollment.billingType) {
        BillingType.Installment -> "Installment"
        BillingType.Subscription -> "Monthly"
        null -> ""
    }
    val subtitle = listOfNotNull(
        enrollment.courseName ?: enrollment.courseId,
        billingLabel.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)) {
            Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd), verticalAlignment = Alignment.CenterVertically) {
                ColoredPhotoAvatar(name = enrollment.studentName ?: "?", size = 52)
                Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
                    Text(
                        enrollment.studentName ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PaymentStatColumn("Paid", enrollment.totalAmountPaid, appColors().success)
                PaymentStatColumn("Due", enrollment.balance, appColors().error)
                PaymentStatColumn("Total", enrollment.totalAmountDue, MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PaymentStatColumn(label: String, amount: Int, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            Formatters.formatInr(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun InstallmentPickerCard(
    installments: List<Installment>,
    selectedId: String?,
    onSelect: (Installment) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
            Text("Select Installment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (installments.isEmpty()) {
                Text("No installments found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                installments.forEach { installment ->
                    InstallmentPickerRow(
                        installment = installment,
                        selected = installment.installmentId == selectedId,
                        onClick = { onSelect(installment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallmentPickerRow(
    installment: Installment,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isPaid = installment.status == InstallmentStatus.Paid
    val (statusLabel, statusColor) = when (installment.status) {
        InstallmentStatus.Paid -> "Paid" to appColors().success
        InstallmentStatus.Overdue -> "Overdue" to appColors().error
        InstallmentStatus.Partial -> "Partial" to appColors().warning
        InstallmentStatus.Unpaid -> "Pending" to appColors().warning
    }
    val rowBg = when {
        selected -> appColors().rowSelected
        isPaid -> appColors().rowPaid
        else -> MaterialTheme.colorScheme.surface
    }
    val dotColor = when (installment.status) {
        InstallmentStatus.Paid -> MaterialTheme.colorScheme.onSurfaceVariant
        InstallmentStatus.Overdue -> appColors().error
        else -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(rowBg)
            .border(
                width = if (selected) appDimens().strokeHairline else appDimens().spacingNone,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                shape = appDimens().fieldShape
            )
            .then(if (!isPaid) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = appDimens().spacingMd, vertical = appDimens().spacing10),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InstallmentStatusDot(color = dotColor, highlighted = installment.status == InstallmentStatus.Overdue)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
            Text(
                "#${installment.installmentNumber} — ${Formatters.formatInr(installment.amountDue)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Due: ${Formatters.formatDateIst(installment.dueDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Pill(statusLabel, statusColor, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun InstallmentStatusDot(color: Color, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .size(if (highlighted) appDimens().spacing14 else appDimens().spacing10)
            .clip(CircleShape)
            .background(if (highlighted) color.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (highlighted) appDimens().spacingXxs else appDimens().strokeHairline,
                color = color,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (highlighted) {
            Box(
                modifier = Modifier
                    .size(appDimens().spacing6)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun PaymentDetailsCard(
    amount: Int,
    onAmountChange: (Int) -> Unit,
    method: PaymentMethod,
    onMethodChange: (PaymentMethod) -> Unit,
    upiRef: String,
    onUpiRefChange: (String) -> Unit,
    paymentDate: String,
    onPaymentDateChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)) {
            FormAmountField(
                amount = amount,
                onAmountChange = onAmountChange,
                fieldColors = fieldColors
            )

            Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                FormLabel("Payment Method")
                PaymentMethodSelector(
                    selected = method,
                    onSelected = onMethodChange
                )
            }

            if (method == PaymentMethod.UPI) {
                FormTextField(
                    label = "UPI Reference (optional)",
                    value = upiRef,
                    onValueChange = onUpiRefChange,
                    placeholder = "e.g. UPI1234567890",
                    fieldColors = fieldColors
                )
            }

            if (method == PaymentMethod.QR) {
                QrPaymentPanel()
            }

            PaymentDateField(
                value = paymentDate,
                onValueChange = onPaymentDateChange,
                fieldColors = fieldColors
            )

            FormTextField(
                label = "Notes (optional)",
                value = notes,
                onValueChange = onNotesChange,
                placeholder = "Any additional notes...",
                singleLine = false,
                minLines = 2,
                fieldColors = fieldColors
            )
        }
    }
}

@Composable
private fun FormAmountField(
    amount: Int,
    onAmountChange: (Int) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
        FormLabel("Amount (₹)")
        OutlinedTextField(
            value = if (amount == 0) "" else amount.toString(),
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(9)
                onAmountChange(digits.toIntOrNull() ?: 0)
            },
            prefix = { Text("₹ ") },
            placeholder = { Text("1000") },
            singleLine = true,
            shape = appDimens().fieldShape,
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Partial amounts allowed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentMethodSelector(
    selected: PaymentMethod,
    onSelected: (PaymentMethod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
    ) {
        FORM_PAYMENT_METHODS.forEach { method ->
            val active = selected == method
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(appDimens().fieldShape)
                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(
                        width = appDimens().strokeHairline,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = appDimens().fieldShape
                    )
                    .clickable { onSelected(method) }
                    .padding(vertical = appDimens().spacing10),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    paymentMethodLabel(method),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun QrPaymentPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = appDimens().iconSizeLg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
            Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeListBox))
            Text("Institute Payment QR", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDateField(
    value: String,
    onValueChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
        FormLabel("Payment Date")
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatPaymentDate(value),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = appDimens().fieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showPicker = true }
            )
        }
    }

    if (showPicker) {
        val initialMillis = runCatching { LocalDate.parse(value) }.getOrNull()
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = LocalDate.now(Formatters.IST)
                        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    return utcTimeMillis <= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(date.toString())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
        FormLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
            minLines = minLines,
            shape = appDimens().fieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionBillingCard(
    billingMonth: String,
    usePackage: Boolean,
    showPackageToggle: Boolean,
    onBillingMonthChange: (String) -> Unit,
    onUsePackageChange: (Boolean) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
            Text("Billing Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            PaymentDateField(
                value = if (billingMonth.isBlank()) "" else "$billingMonth-01",
                onValueChange = { d -> onBillingMonthChange(d.substring(0, 7)) },
                fieldColors = fieldColors
            )
            if (showPackageToggle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Use 3+1 package (4th month bonus)")
                    Switch(checked = usePackage, onCheckedChange = onUsePackageChange)
                }
            }
        }
    }
}

private fun paymentMethodLabel(method: PaymentMethod): String = when (method) {
    PaymentMethod.CASH -> "Cash"
    PaymentMethod.UPI -> "UPI"
    PaymentMethod.QR -> "QR"
    PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
}

private fun formatPaymentDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val d = LocalDate.parse(iso.take(10))
        String.format("%02d/%02d/%04d", d.dayOfMonth, d.monthValue, d.year)
    }.getOrDefault("")
}
