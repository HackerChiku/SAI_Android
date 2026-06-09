package com.saicomputer.sms.feature.enrollments

import com.saicomputer.sms.core.ui.theme.appColors
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.PaymentActionButtons
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus
import com.saicomputer.sms.data.model.EnrollmentTopic
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.data.model.InstallmentStatus
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.PaymentMethod
import com.saicomputer.sms.data.model.PaymentStatus
import com.saicomputer.sms.data.model.TopicDurationUnit
import com.saicomputer.sms.data.model.TopicsSummary
import com.saicomputer.sms.core.ui.theme.appDimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentDetailScreen(
    enrollmentId: String,
    onBack: () -> Unit,
    onRecordPayment: (String) -> Unit,
    snackbarController: SnackbarController,
    viewModel: EnrollmentDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(enrollmentId) { viewModel.load(enrollmentId) }
    LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { snackbarController.show(scope, it) }
    }

    val msg: (String) -> Unit = { snackbarController.show(scope, it) }

    val topBarTitle = when (val s = state) {
        is UiState.Success -> {
            val e = s.data.enrollment
            val student = e.studentName ?: "Student"
            val course = e.courseName ?: e.courseId
            "$student — $course"
        }
        else -> "Enrollment"
    }

    when (val s = state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                EnrollmentDetailHeader(title = topBarTitle, onBack = onBack)
                LoadingSkeleton(Modifier.fillMaxSize())
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                EnrollmentDetailHeader(title = topBarTitle, onBack = onBack)
                ErrorState(s.message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize())
            }
        }
        is UiState.Success -> {
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::manualRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                EnrollmentDetailContent(
                    e = s.data.enrollment,
                    payments = s.data.payments.orEmpty(),
                    canEditInstallments = can(user, "enrollments.editInstallments"),
                    canMarkComplete = can(user, "enrollments.markComplete"),
                    canCancel = can(user, "enrollments.cancel"),
                    canExclude = can(user, "enrollments.setExcludedFromBilling"),
                    canMarkTopics = can(user, "enrollments.topics.markComplete"),
                    canUnmarkTopics = can(user, "enrollments.topics.unmark"),
                    canBackdate = can(user, "system.backdate"),
                    onBack = onBack,
                    topBarTitle = topBarTitle,
                    onRecordPayment = { onRecordPayment(s.data.enrollment.enrollmentId) },
                    viewModel = viewModel,
                    onMessage = msg
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EnrollmentDetailContent(
    e: Enrollment,
    payments: List<Payment>,
    canEditInstallments: Boolean,
    canMarkComplete: Boolean,
    canCancel: Boolean,
    canExclude: Boolean,
    canMarkTopics: Boolean,
    canUnmarkTopics: Boolean,
    canBackdate: Boolean,
    onBack: () -> Unit,
    topBarTitle: String,
    onRecordPayment: () -> Unit,
    viewModel: EnrollmentDetailViewModel,
    onMessage: (String) -> Unit
) {
    var showInstallmentEdit by remember { mutableStateOf(false) }
    var showMarkComplete by remember { mutableStateOf(false) }
    var incompleteTopics by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCancel by remember { mutableStateOf(false) }
    var topicToComplete by remember { mutableStateOf<EnrollmentTopic?>(null) }
    val context = LocalContext.current

    val ongoing = e.enrollmentStatus == EnrollmentStatus.Ongoing
    val isInstallment = e.billingType == BillingType.Installment
    val feePercent = if (e.totalAmountDue > 0) {
        (e.totalAmountPaid * 100 / e.totalAmountDue).coerceIn(0, 100)
    } else 0
    val topicsPercent = e.topicsSummary?.progressPercent ?: 0

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        EnrollmentDetailHeader(title = topBarTitle, onBack = onBack)

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = appDimens().spacingSm),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
            ) {
            EnrollmentSummaryCard(
                enrollment = e,
                feePercent = feePercent,
                topicsPercent = topicsPercent
            )

            if (e.enrollmentFeeWaived && e.waivedFromCourseName != null) {
                EnrollmentFeeWaiverBanner(
                    waivedFromCourseName = e.waivedFromCourseName,
                    modifier = Modifier.padding(horizontal = appDimens().spacingLg)
                )
            }

            if (isInstallment) {
                e.installments?.takeIf { it.isNotEmpty() }?.let { installments ->
                    InstallmentScheduleCard(
                        installments = installments,
                        canEdit = canEditInstallments && ongoing,
                        onEdit = { showInstallmentEdit = true },
                        onPay = onRecordPayment
                    )
                }
            }

            e.topics?.takeIf { it.isNotEmpty() }?.let { topics ->
                TopicsCard(
                    topics = topics,
                    summary = e.topicsSummary,
                    canMark = canMarkTopics && ongoing,
                    canUnmark = canUnmarkTopics && ongoing,
                    onMark = { topicToComplete = it },
                    onUnmark = { viewModel.unmarkTopic(it.enrollmentTopicId, onMessage) }
                )
            }

            if (payments.isNotEmpty()) {
                PaymentHistoryCard(
                    payments = payments,
                    onReceipt = { payment ->
                        val receiptId = payment.receiptId
                        if (receiptId.isNullOrBlank()) {
                            onMessage("No receipt for this payment")
                        } else {
                            viewModel.loadReceipt(
                                receiptId,
                                onSuccess = { receipt ->
                                    val url = receipt.previewUrl ?: receipt.downloadUrl
                                    if (url != null) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    } else {
                                        onMessage("Receipt ${receipt.receiptId}")
                                    }
                                },
                                onError = onMessage
                            )
                        }
                    }
                )
            }

            if (canExclude) {
                BillingExclusionCard(
                    excluded = e.excludedFromBilling,
                    onToggle = { excluded -> viewModel.setExcluded(excluded, onMessage) }
                )
            }

                Spacer(Modifier.height(appDimens().fabScrollClearance))
            }

            if (ongoing) {
                EnrollmentFloatingActions(
                    canMarkComplete = canMarkComplete,
                    canCancel = canCancel,
                    onRecordPayment = onRecordPayment,
                    onMarkComplete = {
                        incompleteTopics = emptyList()
                        showMarkComplete = true
                    },
                    onCancel = { showCancel = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = appDimens().spacingLg, bottom = appDimens().spacingLg)
                )
            }
        }
    }

    if (showInstallmentEdit) {
        InstallmentEditDialog(
            installments = e.installments.orEmpty(),
            totalRequired = e.totalAmountDue,
            ceiling = e.installments?.size?.coerceAtLeast(1)?.let { maxOf(it, 24) } ?: 24,
            startDate = e.startDate,
            onSave = { rows -> showInstallmentEdit = false; viewModel.saveInstallments(rows, onMessage) },
            onDismiss = { showInstallmentEdit = false }
        )
    }

    if (showMarkComplete) {
        MarkCompleteDialog(
            incompleteTopics = incompleteTopics,
            canBackdate = canBackdate,
            onConfirm = { force, backdate, date ->
                showMarkComplete = false
                viewModel.markComplete(
                    force = force,
                    isBackdate = backdate,
                    effectiveActualEndDate = date,
                    onIncompleteTopics = { topics -> incompleteTopics = topics; showMarkComplete = true },
                    onMessage = onMessage
                )
            },
            onDismiss = { showMarkComplete = false }
        )
    }

    if (showCancel) {
        CancelEnrollmentDialog(
            onConfirm = { reason -> showCancel = false; viewModel.cancel(reason, onMessage) },
            onDismiss = { showCancel = false }
        )
    }

    topicToComplete?.let { topic ->
        TopicCompleteDialog(
            topicName = topic.topicName,
            onConfirm = { notes ->
                topicToComplete = null
                viewModel.markTopicComplete(topic.enrollmentTopicId, notes, onMessage)
            },
            onDismiss = { topicToComplete = null }
        )
    }
}

@Composable
private fun EnrollmentDetailHeader(title: String, onBack: () -> Unit) {
    AppTopBarBox {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().spacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
        ) {
            Box(
                modifier = Modifier
                    .size(appDimens().iconSizeXxl)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(appDimens().iconSizeListInner)
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnrollmentSummaryCard(
    enrollment: Enrollment,
    feePercent: Int,
    topicsPercent: Int
) {
    val statusLabel = when (enrollment.enrollmentStatus) {
        EnrollmentStatus.Ongoing -> "Active"
        EnrollmentStatus.Completed -> "Completed"
        EnrollmentStatus.Cancelled -> "Cancelled"
    }
    val statusColor = when (enrollment.enrollmentStatus) {
        EnrollmentStatus.Ongoing -> appColors().success
        EnrollmentStatus.Completed -> appColors().neutral
        EnrollmentStatus.Cancelled -> appColors().error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredPhotoAvatar(
                    name = enrollment.studentName ?: enrollment.courseName ?: "?",
                    size = 56
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
                    Text(
                        enrollment.studentName ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        enrollment.courseFullName ?: enrollment.courseName ?: enrollment.courseId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(statusLabel, statusColor, style = MaterialTheme.typography.labelMedium)
            }

            BillingTypePills(selected = enrollment.billingType)

            Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
                    SummaryField("Start", Formatters.formatDateIst(enrollment.startDate), Modifier.weight(1f))
                    SummaryField(
                        "End / Expected",
                        Formatters.formatDateIst(enrollment.expectedEndDate),
                        Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
                    SummaryField("Effective Fee", Formatters.formatInr(enrollment.effectiveFee), Modifier.weight(1f))
                    SummaryField(
                        "Enrollment Fee",
                        Formatters.formatInr(enrollment.effectiveEnrollmentFee),
                        Modifier.weight(1f)
                    )
                }
            }

            ProgressRow(label = "Fee paid", percent = feePercent, color = MaterialTheme.colorScheme.primary)
            if (enrollment.topicsSummary?.hasTopics == true || !enrollment.topics.isNullOrEmpty()) {
                ProgressRow(label = "Topics completed", percent = topicsPercent, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BillingTypePills(selected: BillingType?) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
        BillingTypePill("Installment", selected == BillingType.Installment)
        BillingTypePill("Monthly", selected == BillingType.Subscription)
    }
}

@Composable
private fun BillingTypePill(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(appDimens().pillShape)
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(
                width = appDimens().strokeHairline,
                color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant,
                shape = appDimens().pillShape
            )
            .padding(horizontal = appDimens().spacing14, vertical = appDimens().spacing6)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProgressRow(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(appDimens().spacing6).clip(appDimens().pillShape),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun InstallmentScheduleCard(
    installments: List<Installment>,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onPay: () -> Unit
) {
    DetailCard(modifier = Modifier.padding(horizontal = appDimens().spacingLg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Installment Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (canEdit) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(appDimens().spacingLg))
                    Spacer(Modifier.size(appDimens().spacingXs))
                    Text("Edit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(appDimens().spacingSm))
        installments.forEachIndexed { index, installment ->
            if (index > 0) Spacer(Modifier.height(appDimens().spacingSm))
            InstallmentScheduleRow(installment = installment, onPay = onPay)
        }
    }
}

@Composable
private fun InstallmentScheduleRow(installment: Installment, onPay: () -> Unit) {
    val rowStyle = when (installment.status) {
        InstallmentStatus.Paid -> InstallmentRowStyle(appColors().rowPaid, "Paid", appColors().success, false)
        InstallmentStatus.Overdue -> InstallmentRowStyle(appColors().rowOverdue, "Overdue", appColors().error, true)
        InstallmentStatus.Partial -> InstallmentRowStyle(appColors().rowPending, "Partial", appColors().warning, true)
        InstallmentStatus.Unpaid -> InstallmentRowStyle(appColors().rowPending, "Pending", appColors().warning, true)
    }
    val bg = rowStyle.background
    val statusLabel = rowStyle.statusLabel
    val statusColor = rowStyle.statusColor
    val showPay = rowStyle.showPay

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(bg)
            .padding(horizontal = appDimens().spacingMd, vertical = appDimens().spacing10),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
            Text(
                "#${installment.installmentNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                Formatters.formatInr(installment.amountDue),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Due: ${Formatters.formatDateIst(installment.dueDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
            Pill(statusLabel, statusColor, style = MaterialTheme.typography.labelMedium)
            if (showPay) {
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(appDimens().spacingSm),
                    modifier = Modifier.height(appDimens().spacing32),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("Pay", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class InstallmentRowStyle(
    val background: Color,
    val statusLabel: String,
    val statusColor: Color,
    val showPay: Boolean
)

@Composable
private fun TopicsCard(
    topics: List<EnrollmentTopic>,
    summary: TopicsSummary?,
    canMark: Boolean,
    canUnmark: Boolean,
    onMark: (EnrollmentTopic) -> Unit,
    onUnmark: (EnrollmentTopic) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val completedCount = summary?.completed ?: topics.count { it.isCompleted }
    val totalCount = summary?.total ?: topics.size
    val percent = summary?.progressPercent
        ?: if (topics.isNotEmpty()) completedCount * 100 / topics.size else 0

    DetailCard(modifier = Modifier.padding(horizontal = appDimens().spacingLg)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Topics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "$completedCount of $totalCount completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
                ) {
                    Text("$percent%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse topics" else "Expand topics",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(appDimens().spacingSm))
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier.fillMaxWidth().height(appDimens().spacing6).clip(appDimens().pillShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = appDimens().spacingMd)) {
                topics.forEachIndexed { index, topic ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = appDimens().spacing10))
                    }
                    TopicListRow(
                        topic = topic,
                        canMark = canMark,
                        canUnmark = canUnmark,
                        onMark = { onMark(topic) },
                        onUnmark = { onUnmark(topic) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicListRow(
    topic: EnrollmentTopic,
    canMark: Boolean,
    canUnmark: Boolean,
    onMark: () -> Unit,
    onUnmark: () -> Unit
) {
    val durationLabel = "${topic.estimatedDurationValue} ${topic.estimatedDurationUnit.labelFor(topic.estimatedDurationValue)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    !topic.isCompleted && canMark -> Modifier.clickable(onClick = onMark)
                    topic.isCompleted && canUnmark -> Modifier.clickable(onClick = onUnmark)
                    else -> Modifier
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (topic.isCompleted) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (topic.isCompleted) appColors().success else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(appDimens().iconSizeListInner)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)) {
            Text(
                topic.topicName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            topic.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .clip(appDimens().pillShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = appDimens().spacingSm, vertical = appDimens().cornerRadiusProgress)
            ) {
                Text(durationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (topic.isCompleted) {
                val completedLine = buildString {
                    append("✓ Completed")
                    topic.completedDate?.let { append(" ${Formatters.formatDateIst(it)}") }
                    topic.completedBy?.let { append(" by $it") }
                    topic.completionNotes?.let { append(" — '$it'") }
                }
                Text(
                    completedLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors().success,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun TopicDurationUnit.labelFor(value: Int): String = when (this) {
    TopicDurationUnit.Weeks -> if (value == 1) "Week" else "Weeks"
    TopicDurationUnit.Months -> if (value == 1) "Month" else "Months"
}

@Composable
private fun PaymentHistoryCard(payments: List<Payment>, onReceipt: (Payment) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val activePayments = payments.filter { it.status != PaymentStatus.Voided }
    val totalPaid = activePayments.sumOf { it.amount }
    val paymentLabel = if (payments.size == 1) "1 payment" else "${payments.size} payments"

    DetailCard(modifier = Modifier.padding(horizontal = appDimens().spacingLg)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Payment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "$paymentLabel · ${Formatters.formatInr(totalPaid)} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse payment history" else "Expand payment history",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = appDimens().spacingMd)) {
                payments.forEachIndexed { index, payment ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(vertical = appDimens().spacingXs)
                        )
                    }
                    PaymentHistoryRow(payment = payment, onReceipt = { onReceipt(payment) })
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryRow(payment: Payment, onReceipt: () -> Unit) {
    val isVoided = payment.status == PaymentStatus.Voided
    val methodLabel = when (payment.paymentMethod) {
        PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
        else -> payment.paymentMethod.name
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrencyText(payment.amount, style = MaterialTheme.typography.titleMedium, bold = true)
            Pill(
                text = if (isVoided) "Voided" else "Paid",
                color = if (isVoided) appColors().error else appColors().success,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            "${Formatters.formatDateIst(payment.paymentDate)} · $methodLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        PaymentActionButtons(
            showReceipt = !payment.receiptId.isNullOrBlank(),
            onViewReceipt = onReceipt
        )
    }
}

@Composable
private fun BillingExclusionCard(excluded: Boolean, onToggle: (Boolean) -> Unit) {
    DetailCard(modifier = Modifier.padding(horizontal = appDimens().spacingLg)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)) {
                Text(
                    "Exclude from billing tracking",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "This enrollment won't appear in pending reports",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = excluded,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = MaterialTheme.colorScheme.tertiary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentFloatingActions(
    canMarkComplete: Boolean,
    canCancel: Boolean,
    onRecordPayment: () -> Unit,
    onMarkComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showActionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { showActionsSheet = true },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = appDimens().spacingXs)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Enrollment actions")
        }
    }

    if (showActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            EnrollmentActionsBottomSheet(
                canMarkComplete = canMarkComplete,
                canCancel = canCancel,
                onRecordPayment = {
                    showActionsSheet = false
                    onRecordPayment()
                },
                onMarkComplete = {
                    showActionsSheet = false
                    onMarkComplete()
                },
                onCancel = {
                    showActionsSheet = false
                    onCancel()
                }
            )
        }
    }
}

@Composable
private fun EnrollmentActionsBottomSheet(
    canMarkComplete: Boolean,
    canCancel: Boolean,
    onRecordPayment: () -> Unit,
    onMarkComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = appDimens().spacingLg)
    ) {
        Text(
            text = "Enrollment actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = appDimens().iconSizeLg, vertical = appDimens().spacingSm)
        )

        EnrollmentActionSheetRow(
            icon = Icons.Outlined.CreditCard,
            label = "Record Payment",
            onClick = onRecordPayment
        )

        if (canMarkComplete) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = appDimens().iconSizeLg)
            )
            EnrollmentActionSheetRow(
                icon = Icons.Outlined.CheckCircle,
                label = "Mark Complete",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onMarkComplete
            )
        }

        if (canCancel) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = appDimens().iconSizeLg)
            )
            EnrollmentActionSheetRow(
                icon = Icons.Outlined.Cancel,
                label = "Cancel Enrollment",
                onClick = onCancel
            )
        }
    }
}

@Composable
private fun EnrollmentActionSheetRow(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.tertiary,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = appDimens().iconSizeLg, vertical = appDimens().spacingLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(appDimens().iconSizeLg)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), content = content)
    }
}
