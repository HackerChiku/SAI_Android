package com.saicomputer.sms.feature.receipts

import com.saicomputer.sms.core.ui.theme.appColors
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.FilterDialogShell
import com.saicomputer.sms.core.ui.FilterDropdown
import com.saicomputer.sms.core.ui.ListDateFilter
import com.saicomputer.sms.core.ui.ListSearchFilterSortBar
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.ReceiptActionButtons
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SubpageTitleBar
import com.saicomputer.sms.core.ui.ResendEmailDialog
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.SortDialog
import com.saicomputer.sms.core.ui.emailStatusColor
import com.saicomputer.sms.data.model.EmailStatus
import com.saicomputer.sms.data.model.ReceiptListItem
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsListScreen(
    user: User? = null,
    onBack: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: ReceiptsViewModel = hiltViewModel()
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
    var resendFor by remember { mutableStateOf<ReceiptListItem?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ReceiptsListHeader(user = user, onBack = onBack)

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
                searchPlaceholder = "Search by student or receipt ID",
                hasActiveFilters = filters.hasActiveFilters,
                hasActiveSort = filters.hasActiveSort,
                onFilterClick = { showFilterDialog = true },
                onSortClick = { showSortDialog = true }
            )

            if (showFilterDialog) {
                ReceiptsFilterDialog(
                    emailStatus = filters.emailStatus,
                    voidedStatus = filters.voidedStatus,
                    fromDate = filters.fromDate,
                    toDate = filters.toDate,
                    onEmailStatusChange = viewModel::onEmailStatusChange,
                    onVoidedStatusChange = viewModel::onVoidedStatusChange,
                    onFromDateChange = viewModel::onFromDateChange,
                    onToDateChange = viewModel::onToDateChange,
                    onClear = viewModel::clearFilterFields,
                    onDismiss = { showFilterDialog = false }
                )
            }

            if (showSortDialog) {
                SortDialog(
                    title = "Sort",
                    options = ReceiptSort.OPTIONS,
                    optionLabels = ReceiptSort.LABELS,
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
                                title = "No receipts match your filters",
                                actionLabel = "Clear filters",
                                onAction = viewModel::clearFilters,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyState(title = "No receipts", modifier = Modifier.fillMaxSize())
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = appDimens().spacingLg),
                            verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
                        ) {
                            items(s.data, key = { it.receiptId }) { receipt ->
                                ReceiptCard(
                                    receipt = receipt,
                                    onView = {
                                        viewModel.loadReceipt(
                                            receipt.receiptId,
                                            onSuccess = { detail ->
                                                val url = detail.previewUrl ?: detail.downloadUrl
                                                if (url != null) {
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    )
                                                } else {
                                                    snackbarController.show(
                                                        scope,
                                                        "Receipt ${detail.receiptId}"
                                                    )
                                                }
                                            },
                                            onError = { snackbarController.show(scope, it) }
                                        )
                                    },
                                    onResend = { resendFor = receipt }
                                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptsFilterDialog(
    emailStatus: String,
    voidedStatus: String,
    fromDate: String?,
    toDate: String?,
    onEmailStatusChange: (String) -> Unit,
    onVoidedStatusChange: (String) -> Unit,
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
            displayLabel = ReceiptEmailStatusFilter.LABELS[emailStatus] ?: "All Email Status",
            options = ReceiptEmailStatusFilter.OPTIONS,
            optionLabels = ReceiptEmailStatusFilter.LABELS,
            selected = emailStatus,
            onSelected = onEmailStatusChange,
            modifier = Modifier.fillMaxWidth()
        )
        FilterDropdown(
            displayLabel = ReceiptVoidedFilter.LABELS[voidedStatus] ?: "All Receipts",
            options = ReceiptVoidedFilter.OPTIONS,
            optionLabels = ReceiptVoidedFilter.LABELS,
            selected = voidedStatus,
            onSelected = onVoidedStatusChange,
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
private fun ReceiptsListHeader(user: User?, onBack: () -> Unit) {
    SubpageTitleBar(title = "Receipts", onBack = onBack, user = user)
}

@Composable
private fun ReceiptCard(
    receipt: ReceiptListItem,
    onView: () -> Unit,
    onResend: () -> Unit
) {
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
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)
                ) {
                    Text(
                        receipt.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${receipt.receiptId} · ${Formatters.formatDateIst(receipt.generatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CurrencyText(
                        receipt.amount,
                        style = MaterialTheme.typography.titleMedium,
                        bold = true,
                        modifier = Modifier.padding(top = appDimens().spacingXs)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingXs)) {
                        Pill(
                            text = emailStatusLabel(receipt.emailStatus),
                            color = emailStatusPillColor(receipt.emailStatus),
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (receipt.voidedWithPayment) {
                            Pill(text = "Voided", color = appColors().error, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            ReceiptActionButtons(onView = onView, onResend = onResend)
        }
    }
}

private fun emailStatusLabel(status: EmailStatus): String = when (status) {
    EmailStatus.NotSent -> "Not Sent"
    EmailStatus.NotApplicable -> "N/A"
    else -> status.name
}

@Composable
private fun emailStatusPillColor(status: EmailStatus) = when (status) {
    EmailStatus.NotSent, EmailStatus.NotApplicable -> appColors().neutral
    else -> emailStatusColor(status)
}
