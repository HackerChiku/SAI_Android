package com.saicomputer.sms.feature.payments

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineLight
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.data.model.InstallmentStatus
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.PaymentMethod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private val BackdateOrange = Color(0xFFEA580C)
private val BackdateOrangeTint = Color(0xFFFFF7ED)
private val SelectedInstallmentTint = Color(0xFFE8EDF8)
private val PaidRowTint = Color(0xFFF7F9FC)

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
        focusedBorderColor = OutlineLight,
        unfocusedBorderColor = OutlineVariantLight,
        focusedContainerColor = BaseWhite,
        unfocusedContainerColor = BaseWhite,
        focusedPlaceholderColor = OnSurfaceVariantLightColor,
        unfocusedPlaceholderColor = OnSurfaceVariantLightColor
    )

    Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        PaymentFormHeader(onBack = onBack)

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
        }

        Button(
            onClick = { viewModel.submit(onSuccess = onRecorded, onMessage = msg) },
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = FieldShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandRed,
                contentColor = BaseWhite,
                disabledContainerColor = BrandRed.copy(alpha = 0.4f)
            )
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = BaseWhite
                )
            } else {
                Text("Record Payment", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun PaymentFormHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = BaseWhite)
        }
        Text(
            "Record Payment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ColoredPhotoAvatar(name = enrollment.studentName ?: "?", size = 52)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        enrollment.studentName ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PaymentStatColumn("Paid", enrollment.totalAmountPaid, StatusEmerald)
                PaymentStatColumn("Due", enrollment.balance, StatusRed)
                PaymentStatColumn("Total", enrollment.totalAmountDue, MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PaymentStatColumn(label: String, amount: Int, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantLightColor)
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Select Installment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (installments.isEmpty()) {
                Text("No installments found.", color = OnSurfaceVariantLightColor)
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
        InstallmentStatus.Paid -> "Paid" to StatusEmerald
        InstallmentStatus.Overdue -> "Overdue" to StatusRed
        InstallmentStatus.Partial -> "Partial" to BackdateOrange
        InstallmentStatus.Unpaid -> "Pending" to BackdateOrange
    }
    val rowBg = when {
        selected -> SelectedInstallmentTint
        isPaid -> PaidRowTint
        else -> BaseWhite
    }
    val dotColor = when (installment.status) {
        InstallmentStatus.Paid -> OnSurfaceVariantLightColor
        InstallmentStatus.Overdue -> StatusRed
        else -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(rowBg)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) BrandBlue.copy(alpha = 0.25f) else Color.Transparent,
                shape = FieldShape
            )
            .then(if (!isPaid) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InstallmentStatusDot(color = dotColor, highlighted = installment.status == InstallmentStatus.Overdue)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "#${installment.installmentNumber} — ${Formatters.formatInr(installment.amountDue)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Due: ${Formatters.formatDateIst(installment.dueDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariantLightColor
            )
        }
        Pill(statusLabel, statusColor, fontSize = 10.sp)
    }
}

@Composable
private fun InstallmentStatusDot(color: Color, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .size(if (highlighted) 14.dp else 10.dp)
            .clip(CircleShape)
            .background(if (highlighted) color.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (highlighted) 2.dp else 1.dp,
                color = color,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (highlighted) {
            Box(
                modifier = Modifier
                    .size(6.dp)
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FormAmountField(
                amount = amount,
                onAmountChange = onAmountChange,
                fieldColors = fieldColors
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            shape = FieldShape,
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Partial amounts allowed.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariantLightColor
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FORM_PAYMENT_METHODS.forEach { method ->
            val active = selected == method
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(FieldShape)
                    .background(if (active) BrandBlue else BaseWhite)
                    .border(
                        width = 1.dp,
                        color = if (active) BrandBlue else OutlineVariantLight,
                        shape = FieldShape
                    )
                    .clickable { onSelected(method) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    paymentMethodLabel(method),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) BaseWhite else MaterialTheme.colorScheme.onSurface
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
            .clip(FieldShape)
            .background(OffWhite)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(48.dp))
            Text("Institute Payment QR", style = MaterialTheme.typography.bodyMedium, color = BrandBlue)
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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel("Payment Date")
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatPaymentDate(value),
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = OnSurfaceVariantLightColor)
                },
                shape = FieldShape,
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
            minLines = minLines,
            shape = FieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BackdateEntryCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    date: String?,
    onDateChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(BackdateOrange.copy(alpha = 0.65f))
            .background(BackdateOrangeTint, FieldShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = BackdateOrange, modifier = Modifier.size(26.dp))
            Text(
                "Record as backdated entry",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = BackdateOrange,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BaseWhite,
                    checkedTrackColor = BackdateOrange,
                    uncheckedThumbColor = BaseWhite,
                    uncheckedTrackColor = OutlineVariantLight
                )
            )
        }
        if (enabled) {
            BackdateDateField(value = date, onValueChange = onDateChange)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = BackdateOrange, modifier = Modifier.size(18.dp))
                Text(
                    "No confirmation email will be sent automatically. Receipt PDF will still be generated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BackdateOrange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackdateDateField(value: String?, onValueChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BackdateOrange,
        unfocusedBorderColor = BackdateOrange.copy(alpha = 0.7f),
        focusedContainerColor = BackdateOrangeTint,
        unfocusedContainerColor = BackdateOrangeTint,
        focusedTextColor = BackdateOrange,
        unfocusedTextColor = BackdateOrange
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatPaymentDate(value),
            onValueChange = {},
            readOnly = true,
            shape = FieldShape,
            colors = colors,
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

    if (showPicker) {
        val initialMillis = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
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
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(picked.toString())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState)
        }
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    val strokeWidth = 1.5.dp.toPx()
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    val corner = 12.dp.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth, pathEffect = dash),
        cornerRadius = CornerRadius(corner, corner)
    )
}
