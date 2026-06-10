package com.saicomputer.sms.feature.payments

import com.saicomputer.sms.core.ui.theme.appColors
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.FilterDialogShell
import com.saicomputer.sms.core.ui.FilterDropdown
import com.saicomputer.sms.core.ui.ListDateFilter
import com.saicomputer.sms.core.ui.ListSearchFilterSortBar
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.PaymentActionButtons
import com.saicomputer.sms.core.ui.SortDialog
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.ProfileMenuButton
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.PaymentListItem
import com.saicomputer.sms.data.model.PaymentMethod
import com.saicomputer.sms.data.model.PaymentStatus
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsListScreen(
    user: User? = null,
    snackbarController: SnackbarController,
    viewModel: PaymentsListViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val displayItems by viewModel.displayItems.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { message ->
            snackbarController.show(scope, message)
        }
    }
    val context = LocalContext.current
    val canVoid = can(user, "payments.void")
    val canEditBilling = can(user, "payments.editBillingMonth")

    var voidTarget by remember { mutableStateOf<PaymentListItem?>(null) }
    var editBillingTarget by remember { mutableStateOf<PaymentListItem?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        PaymentsListHeader(user = user)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = appDimens().spacingLg)
        ) {
            Spacer(Modifier.height(appDimens().spacingMd))

            ListSearchFilterSortBar(
                search = filters.search,
                onSearchChange = viewModel::onSearchChange,
                searchPlaceholder = "Search by student or course",
                hasActiveFilters = filters.hasActiveFilters,
                hasActiveSort = filters.hasActiveSort,
                onFilterClick = { showFilterDialog = true },
                onSortClick = { showSortDialog = true }
            )

            if (showFilterDialog) {
                PaymentsFilterDialog(
                    status = filters.status,
                    paymentMethod = filters.paymentMethod,
                    fromDate = filters.fromDate,
                    toDate = filters.toDate,
                    onStatusChange = viewModel::onStatusChange,
                    onPaymentMethodChange = viewModel::onPaymentMethodChange,
                    onFromDateChange = viewModel::onFromDateChange,
                    onToDateChange = viewModel::onToDateChange,
                    onClear = viewModel::clearFilterFields,
                    onDismiss = { showFilterDialog = false }
                )
            }

            if (showSortDialog) {
                SortDialog(
                    title = "Sort",
                    options = PaymentSort.OPTIONS,
                    optionLabels = PaymentSort.LABELS,
                    selected = filters.sort,
                    onSelected = viewModel::onSortChange,
                    onDismiss = { showSortDialog = false }
                )
            }

            Spacer(Modifier.height(appDimens().spacingMd))

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::manualRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val s = displayItems) {
                    is UiState.Loading -> LoadingSkeleton(modifier = Modifier.fillMaxSize())
                    is UiState.Error -> ErrorState(
                        message = s.message,
                        onRetry = { viewModel.load(force = true) },
                        modifier = Modifier.fillMaxSize()
                    )
                    is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        if (viewModel.hasActiveClientFilters) {
                            EmptyState(
                                title = "No payments match your filters",
                                actionLabel = "Clear filters",
                                onAction = viewModel::clearFilters,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyState(
                                title = "No payments recorded yet",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = appDimens().spacingLg),
                            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentsFilterDialog(
    status: String,
    paymentMethod: String,
    fromDate: String?,
    toDate: String?,
    onStatusChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onFromDateChange: (String?) -> Unit,
    onToDateChange: (String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    FilterDialogShell(
        title = "Filters",
        onClear = onClear,
        onDismiss = onDismiss
    ) {
        FilterDropdown(
            displayLabel = PaymentStatusFilter.LABELS[status] ?: "All Status",
            options = PaymentStatusFilter.OPTIONS,
            optionLabels = PaymentStatusFilter.LABELS,
            selected = status,
            onSelected = onStatusChange,
            modifier = Modifier.fillMaxWidth()
        )
        FilterDropdown(
            displayLabel = PaymentMethodFilter.LABELS[paymentMethod] ?: "All Methods",
            options = PaymentMethodFilter.OPTIONS,
            optionLabels = PaymentMethodFilter.LABELS,
            selected = paymentMethod,
            onSelected = onPaymentMethodChange,
            modifier = Modifier.fillMaxWidth()
        )
        ListDateFilter(
            label = "From date",
            date = fromDate,
            onDateChange = onFromDateChange,
            modifier = Modifier.fillMaxWidth()
        )
        ListDateFilter(
            label = "To date",
            date = toDate,
            onDateChange = onToDateChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PaymentsListHeader(user: User?) {
    AppTopBarBox {
        AppTitleBarRow(
            leading = {
                Text(
                    "Payment History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            },
            actions = {
                ProfileMenuButton(user = user)
            }
        )
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
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingMd),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredPhotoAvatar(name = item.studentName, size = 40)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(appDimens().spacing6),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.studentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (showSessionBadge) {
                            Pill(
                                text = REGISTRATION_SESSION_LABELS[item.registrationSession] ?: "",
                                color = appColors().info,
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    }
                    if (item.courseName.isNotBlank()) {
                        Text(
                            item.courseName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        buildString {
                            append(Formatters.formatDateIst(payment.paymentDate))
                            append(" · ")
                            append(methodLabel)
                            billingMonth?.let {
                                append(" · Billing: ")
                                append(Formatters.formatBillingMonth(it))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
                    CurrencyText(
                        payment.amount,
                        style = MaterialTheme.typography.titleMedium,
                        bold = true
                    )
                    Pill(
                        text = if (isVoided) "Voided" else "Paid",
                        color = if (isVoided) appColors().error else appColors().success,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            PaymentActionButtons(
                showReceipt = !payment.receiptId.isNullOrBlank(),
                onViewReceipt = onViewReceipt,
                showVoid = canVoid && !isVoided,
                onVoid = onVoid,
                showEditMonth = showEditMonth,
                onEditBillingMonth = onEditBillingMonth
            )
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
                Spacer(Modifier.height(appDimens().spacingSm))
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
            ) { Text("Void", color = MaterialTheme.colorScheme.tertiary) }
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
            Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
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
            ) { Text("Save", color = MaterialTheme.colorScheme.primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
