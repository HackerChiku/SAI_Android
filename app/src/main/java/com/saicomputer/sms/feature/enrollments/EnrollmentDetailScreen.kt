package com.saicomputer.sms.feature.enrollments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Topic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.DateText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.ListItemCard
import com.saicomputer.sms.core.ui.ListItemIconBox
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.TopicsProgressBar
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandGold
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus
import com.saicomputer.sms.data.model.EnrollmentTopic
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.data.model.InstallmentStatus
import com.saicomputer.sms.data.model.TopicsSummary

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
    val scope = rememberCoroutineScope()
    LaunchedEffect(enrollmentId) { viewModel.load(enrollmentId) }

    val msg: (String) -> Unit = { snackbarController.show(scope, it) }

    val topBarTitle = when (val s = state) {
        is UiState.Success -> s.data.enrollment.courseName ?: "Enrollment"
        else -> "Enrollment"
    }

    when (val s = state) {
        is UiState.Loading -> {
            Scaffold(topBar = { SmsTopBar(title = topBarTitle, onBack = onBack) }) { padding ->
                LoadingSkeleton(Modifier.fillMaxSize().padding(padding))
            }
        }
        is UiState.Error -> {
            Scaffold(topBar = { SmsTopBar(title = topBarTitle, onBack = onBack) }) { padding ->
                ErrorState(s.message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize().padding(padding))
            }
        }
        is UiState.Success -> {
            EnrollmentDetailContent(
                e = s.data.enrollment,
                canEditInstallments = can(user, "enrollments.editInstallments"),
                canMarkComplete = can(user, "enrollments.markComplete"),
                canCancel = can(user, "enrollments.cancel"),
                canExclude = can(user, "enrollments.setExcludedFromBilling"),
                canMarkTopics = can(user, "enrollments.topics.markComplete"),
                canUnmarkTopics = can(user, "enrollments.topics.unmark"),
                canBackdate = can(user, "system.backdate"),
                canSubExtend = can(user, "subscriptions.extend"),
                onBack = onBack,
                topBarTitle = topBarTitle,
                onRecordPayment = { onRecordPayment(s.data.enrollment.enrollmentId) },
                viewModel = viewModel,
                onMessage = msg
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EnrollmentDetailContent(
    e: Enrollment,
    canEditInstallments: Boolean,
    canMarkComplete: Boolean,
    canCancel: Boolean,
    canExclude: Boolean,
    canMarkTopics: Boolean,
    canUnmarkTopics: Boolean,
    canBackdate: Boolean,
    canSubExtend: Boolean,
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
    var showExtend by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    val actionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val ongoing = e.enrollmentStatus == EnrollmentStatus.Ongoing
    val isInstallment = e.billingType == BillingType.Installment
    val isSubscription = e.billingType == BillingType.Subscription

    val showFab = ongoing

    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = actionSheetState
        ) {
            EnrollmentActionsSheet(
                excludedFromBilling = e.excludedFromBilling,
                isInstallment = isInstallment,
                isSubscription = isSubscription,
                canEditInstallments = canEditInstallments,
                canSubExtend = canSubExtend,
                canExclude = canExclude,
                canMarkComplete = canMarkComplete,
                canCancel = canCancel,
                onRecordPayment = {
                    showActions = false
                    onRecordPayment()
                },
                onEditInstallments = {
                    showActions = false
                    showInstallmentEdit = true
                },
                onExtendSubscription = {
                    showActions = false
                    showExtend = true
                },
                onToggleBillingExclusion = {
                    showActions = false
                    viewModel.setExcluded(!e.excludedFromBilling, onMessage)
                },
                onMarkComplete = {
                    showActions = false
                    incompleteTopics = emptyList()
                    showMarkComplete = true
                },
                onCancel = {
                    showActions = false
                    showCancel = true
                }
            )
        }
    }

    Scaffold(
        topBar = { SmsTopBar(title = topBarTitle, onBack = onBack) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = { showActions = true }) {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "Enrollment actions")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnrollmentHeroHeader(enrollment = e)

            if (e.enrollmentFeeWaived && e.waivedFromCourseName != null) {
                EnrollmentFeeWaiverBanner(
                    waivedFromCourseName = e.waivedFromCourseName,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            DetailSection(
                title = "Billing",
                icon = Icons.Outlined.Payments,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Paid / Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CurrencyText(e.totalAmountPaid, bold = true)
                        Text(" / ", style = MaterialTheme.typography.bodyMedium)
                        CurrencyText(e.totalAmountDue)
                    }
                }
                if (e.balance > 0) {
                    DetailInfoRow("Balance", Formatters.formatInr(e.balance))
                }
                if (isSubscription) {
                    e.paidThroughDate?.let { DetailInfoRow("Paid through", Formatters.formatDateIst(it)) }
                    e.monthlyFee?.let { DetailInfoRow("Monthly fee", Formatters.formatInr(it)) }
                    if (e.isPendingCurrentMonth) {
                        GenericBadge("Pending this month", StatusAmber)
                    }
                }
                if (isInstallment) {
                    DetailInfoRow("Max installments", e.installments?.size?.toString() ?: "—")
                }
            }

            if (isInstallment) {
                e.installments?.takeIf { it.isNotEmpty() }?.let { installments ->
                    DetailSection(
                        title = "Installments (${installments.size})",
                        icon = Icons.Outlined.Payments,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        installments.forEachIndexed { index, installment ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                            InstallmentRow(installment)
                        }
                    }
                }
            }

            DetailSection(
                title = "Schedule",
                icon = Icons.Outlined.CalendarMonth,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                DetailInfoRow("Start date", Formatters.formatDateIst(e.startDate))
                DetailInfoRow("Expected end", Formatters.formatDateIst(e.expectedEndDate))
                e.actualEndDate?.let { DetailInfoRow("Actual end", Formatters.formatDateIst(it)) }
            }

            e.topics?.takeIf { it.isNotEmpty() }?.let { topics ->
                CollapsibleTopicsSection(
                    topics = topics,
                    summary = e.topicsSummary,
                    canMark = canMarkTopics && ongoing,
                    canUnmark = canUnmarkTopics && ongoing,
                    onMark = { topicToComplete = it },
                    onUnmark = { viewModel.unmarkTopic(it.enrollmentTopicId, onMessage) },
                    modifier = Modifier.padding(horizontal = 16.dp)
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
            onConfirm = { notes -> topicToComplete = null; viewModel.markTopicComplete(topic.enrollmentTopicId, notes, onMessage) },
            onDismiss = { topicToComplete = null }
        )
    }

    if (showExtend) {
        ExtendSubscriptionDialog(
            onConfirm = { months -> showExtend = false; viewModel.extendSubscription(months, onMessage) },
            onDismiss = { showExtend = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnrollmentHeroHeader(enrollment: Enrollment) {
    val statusColor = when (enrollment.enrollmentStatus) {
        EnrollmentStatus.Ongoing -> StatusEmerald
        EnrollmentStatus.Completed -> StatusGray
        EnrollmentStatus.Cancelled -> StatusRed
    }
    val billingColor = when (enrollment.billingType) {
        BillingType.Installment -> BrandBlue
        BillingType.Subscription -> BrandGold
        null -> StatusGray
    }

    ListItemCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                ListItemIconBox(icon = Icons.AutoMirrored.Outlined.MenuBook)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        enrollment.courseName ?: enrollment.courseId,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    enrollment.courseFullName?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    enrollment.studentName?.let { name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        enrollment.enrollmentId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GenericBadge(enrollment.enrollmentStatus.name, statusColor)
                enrollment.billingType?.let { GenericBadge(it.name, billingColor) }
                if (enrollment.excludedFromBilling) GenericBadge("Billing excluded", StatusAmber)
                if (enrollment.isBackdate) GenericBadge("Backdated", StatusAmber)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Paid", amount = enrollment.totalAmountPaid, emphasized = true)
                VerticalStatDivider()
                StatItem(label = "Due", amount = enrollment.totalAmountDue)
                VerticalStatDivider()
                StatItem(label = "Balance", amount = enrollment.balance)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, amount: Int, emphasized: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CurrencyText(
            amount,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            bold = emphasized
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalStatDivider() {
    VerticalDivider(
        modifier = Modifier.height(28.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun CollapsibleTopicsSection(
    topics: List<EnrollmentTopic>,
    summary: TopicsSummary?,
    canMark: Boolean,
    canUnmark: Boolean,
    onMark: (EnrollmentTopic) -> Unit,
    onUnmark: (EnrollmentTopic) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val completedCount = summary?.completed ?: topics.count { it.isCompleted }
    val totalCount = summary?.total ?: topics.size
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "topicsExpand")

    ListItemCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Topic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Topics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$completedCount of $totalCount completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse topics" else "Expand topics",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            SpacerBetweenHeaderAndBody(expanded, summary, completedCount, totalCount)

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topics.forEach { topic ->
                        TopicRow(
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
}

@Composable
private fun SpacerBetweenHeaderAndBody(
    expanded: Boolean,
    summary: TopicsSummary?,
    completedCount: Int,
    totalCount: Int
) {
    if (!expanded) {
        TopicsProgressBar(
            completed = completedCount,
            total = totalCount,
            modifier = Modifier.padding(top = 12.dp),
            showLabel = false
        )
    } else {
        summary?.let {
            TopicsProgressBar(
                completed = it.completed,
                total = it.total,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun TopicRow(
    topic: EnrollmentTopic,
    canMark: Boolean,
    canUnmark: Boolean,
    onMark: () -> Unit,
    onUnmark: () -> Unit
) {
    ListItemCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(topic.topicName, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (topic.isCompleted) {
                    Text(
                        "Completed ${Formatters.formatDateIst(topic.completedDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "${topic.estimatedDurationValue} ${topic.estimatedDurationUnit.name.lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            when {
                topic.isCompleted && canUnmark -> {
                    TextButton(onClick = onUnmark) { Text("Undo", fontSize = 12.sp) }
                }
                topic.isCompleted -> GenericBadge("Done", StatusEmerald)
                canMark -> TextButton(onClick = onMark) { Text("Complete", fontSize = 12.sp) }
                else -> GenericBadge("Pending", StatusAmber)
            }
        }
    }
}

@Composable
private fun InstallmentRow(installment: Installment) {
    val statusColor = when (installment.status) {
        InstallmentStatus.Paid -> StatusEmerald
        InstallmentStatus.Partial -> StatusAmber
        InstallmentStatus.Overdue -> StatusRed
        InstallmentStatus.Unpaid -> StatusGray
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Installment #${installment.installmentNumber}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            DateText(
                installment.dueDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CurrencyText(installment.amountPaid, bold = true)
                Text(" / ", style = MaterialTheme.typography.bodySmall)
                CurrencyText(installment.amountDue, style = MaterialTheme.typography.bodySmall)
            }
            GenericBadge(installment.status.name, statusColor)
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ListItemCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(18.dp).height(18.dp)
                )
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EnrollmentActionsSheet(
    excludedFromBilling: Boolean,
    isInstallment: Boolean,
    isSubscription: Boolean,
    canEditInstallments: Boolean,
    canSubExtend: Boolean,
    canExclude: Boolean,
    canMarkComplete: Boolean,
    canCancel: Boolean,
    onRecordPayment: () -> Unit,
    onEditInstallments: () -> Unit,
    onExtendSubscription: () -> Unit,
    onToggleBillingExclusion: () -> Unit,
    onMarkComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Actions",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        EnrollmentActionItem(
            label = "Record Payment",
            icon = Icons.Outlined.Payments,
            onClick = onRecordPayment
        )
        if (isInstallment && canEditInstallments) {
            EnrollmentActionItem(
                label = "Edit Installments",
                icon = Icons.Outlined.Edit,
                onClick = onEditInstallments
            )
        }
        if (isSubscription && canSubExtend) {
            EnrollmentActionItem(
                label = "Extend Subscription",
                icon = Icons.Outlined.EventRepeat,
                onClick = onExtendSubscription
            )
        }
        if (canExclude) {
            EnrollmentActionItem(
                label = if (excludedFromBilling) "Include in billing" else "Exclude from billing",
                icon = Icons.Outlined.Block,
                onClick = onToggleBillingExclusion
            )
        }
        if (canMarkComplete) {
            EnrollmentActionItem(
                label = "Mark Complete",
                icon = Icons.Outlined.CheckCircle,
                onClick = onMarkComplete
            )
        }
        if (canCancel) {
            EnrollmentActionItem(
                label = "Cancel Enrollment",
                icon = Icons.Outlined.Block,
                destructive = true,
                onClick = onCancel
            )
        }
    }
}

@Composable
private fun EnrollmentActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    val color = if (destructive) StatusRed else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (destructive) StatusRed else MaterialTheme.colorScheme.onSurface
        )
    }
}
