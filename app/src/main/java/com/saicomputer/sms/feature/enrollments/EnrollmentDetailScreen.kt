package com.saicomputer.sms.feature.enrollments

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.DateText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.TopicsProgressBar
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus

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

    Scaffold(topBar = { SmsTopBar(title = "Enrollment", onBack = onBack) }) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize().padding(padding))
            is UiState.Error -> ErrorState(s.message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize().padding(padding))
            is UiState.Success -> {
                val e = s.data.enrollment
                EnrollmentDetailContent(
                    e = e,
                    canEditInstallments = can(user, "enrollments.editInstallments"),
                    canMarkComplete = can(user, "enrollments.markComplete"),
                    canCancel = can(user, "enrollments.cancel"),
                    canExclude = can(user, "enrollments.setExcludedFromBilling"),
                    canMarkTopics = can(user, "enrollments.topics.markComplete"),
                    canUnmarkTopics = can(user, "enrollments.topics.unmark"),
                    canBackdate = can(user, "system.backdate"),
                    canSubExtend = can(user, "subscriptions.extend"),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onRecordPayment = { onRecordPayment(e.enrollmentId) },
                    viewModel = viewModel,
                    onMessage = msg
                )
            }
        }
    }
}

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
    modifier: Modifier,
    onRecordPayment: () -> Unit,
    viewModel: EnrollmentDetailViewModel,
    onMessage: (String) -> Unit
) {
    var showInstallmentEdit by remember { mutableStateOf(false) }
    var showMarkComplete by remember { mutableStateOf(false) }
    var incompleteTopics by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCancel by remember { mutableStateOf(false) }
    var topicToComplete by remember { mutableStateOf<com.saicomputer.sms.data.model.EnrollmentTopic?>(null) }
    var showExtend by remember { mutableStateOf(false) }

    val ongoing = e.enrollmentStatus == EnrollmentStatus.Ongoing
    val isInstallment = e.billingType == BillingType.Installment

    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(e.courseName ?: e.courseId, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        e.studentName?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenericBadge(e.enrollmentStatus.name)
            e.billingType?.let { GenericBadge(it.name) }
            if (e.excludedFromBilling) GenericBadge("Billing excluded")
            if (e.isBackdate) GenericBadge("Backdated")
        }

        if (e.enrollmentFeeWaived && e.waivedFromCourseName != null) {
            EnrollmentFeeWaiverBanner(e.waivedFromCourseName)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                InfoLine("Start", Formatters.formatDateIst(e.startDate))
                InfoLine("Expected end", Formatters.formatDateIst(e.expectedEndDate))
                e.actualEndDate?.let { InfoLine("Actual end", Formatters.formatDateIst(it)) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paid / Due")
                    Row {
                        CurrencyText(e.totalAmountPaid, bold = true)
                        Text(" / ")
                        CurrencyText(e.totalAmountDue)
                    }
                }
                if (e.billingType == BillingType.Subscription) {
                    e.paidThroughDate?.let { InfoLine("Paid through", Formatters.formatDateIst(it)) }
                    if (e.isPendingCurrentMonth) GenericBadge("Pending this month")
                }
            }
        }

        // Topics checklist
        e.topics?.let { topics ->
            if (topics.isNotEmpty()) {
                e.topicsSummary?.let { TopicsProgressBar(it.completed, it.total) }
                EnrollmentTopicsChecklist(
                    topics = topics,
                    canMark = canMarkTopics && ongoing,
                    canUnmark = canUnmarkTopics && ongoing,
                    onMark = { topicToComplete = it },
                    onUnmark = { viewModel.unmarkTopic(it.enrollmentTopicId, onMessage) }
                )
            }
        }

        // Installments
        if (isInstallment) {
            e.installments?.let { list ->
                Text("Installments", style = MaterialTheme.typography.titleMedium)
                list.forEach { inst ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("#${inst.installmentNumber}", fontWeight = FontWeight.SemiBold)
                                DateText(inst.dueDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row {
                                    CurrencyText(inst.amountPaid, bold = true)
                                    Text(" / ")
                                    CurrencyText(inst.amountDue)
                                }
                                GenericBadge(inst.status.name)
                            }
                        }
                    }
                }
                if (canEditInstallments && ongoing) {
                    OutlinedButton(onClick = { showInstallmentEdit = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Edit Installments")
                    }
                }
            }
        }

        HorizontalDivider()

        // Actions
        if (ongoing) {
            OutlinedButton(onClick = onRecordPayment, modifier = Modifier.fillMaxWidth()) { Text("Record Payment") }
            if (e.billingType == BillingType.Subscription && canSubExtend) {
                OutlinedButton(onClick = { showExtend = true }, modifier = Modifier.fillMaxWidth()) { Text("Extend Subscription") }
            }
            if (canExclude) {
                OutlinedButton(
                    onClick = { viewModel.setExcluded(!e.excludedFromBilling, onMessage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (e.excludedFromBilling) "Include in billing" else "Exclude from billing")
                }
            }
            if (canMarkComplete) {
                OutlinedButton(onClick = { incompleteTopics = emptyList(); showMarkComplete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mark Complete")
                }
            }
            if (canCancel) {
                TextButton(onClick = { showCancel = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel Enrollment", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
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

@Composable
private fun EnrollmentTopicsChecklist(
    topics: List<com.saicomputer.sms.data.model.EnrollmentTopic>,
    canMark: Boolean,
    canUnmark: Boolean,
    onMark: (com.saicomputer.sms.data.model.EnrollmentTopic) -> Unit,
    onUnmark: (com.saicomputer.sms.data.model.EnrollmentTopic) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Topics", style = MaterialTheme.typography.titleMedium)
        topics.forEach { topic ->
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(topic.topicName, fontWeight = FontWeight.SemiBold)
                        if (topic.isCompleted) {
                            Text(
                                "Completed ${Formatters.formatDateIst(topic.completedDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (topic.isCompleted) {
                        if (canUnmark) TextButton(onClick = { onUnmark(topic) }) { Text("Undo") }
                        else GenericBadge("Done")
                    } else if (canMark) {
                        TextButton(onClick = { onMark(topic) }) { Text("Complete") }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}
