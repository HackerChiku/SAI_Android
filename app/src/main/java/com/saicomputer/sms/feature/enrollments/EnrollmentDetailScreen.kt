package com.saicomputer.sms.feature.enrollments

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
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
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.core.ui.theme.StatusRed
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

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)
private val BackdateOrange = Color(0xFFEA580C)
private val PaidRowTint = Color(0xFFD1FAE5)
private val OverdueRowTint = Color(0xFFFEE2E2)
private val PendingRowTint = Color(0xFFFFF7ED)

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
            Column(Modifier.fillMaxSize().background(OffWhite)) {
                EnrollmentDetailHeader(title = topBarTitle, onBack = onBack)
                LoadingSkeleton(Modifier.fillMaxSize())
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(OffWhite)) {
                EnrollmentDetailHeader(title = topBarTitle, onBack = onBack)
                ErrorState(s.message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize())
            }
        }
        is UiState.Success -> {
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

    Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        EnrollmentDetailHeader(title = topBarTitle, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnrollmentSummaryCard(
                enrollment = e,
                feePercent = feePercent,
                topicsPercent = topicsPercent
            )

            if (e.enrollmentFeeWaived && e.waivedFromCourseName != null) {
                EnrollmentFeeWaiverBanner(
                    waivedFromCourseName = e.waivedFromCourseName,
                    modifier = Modifier.padding(horizontal = 16.dp)
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
        }

        if (ongoing) {
            EnrollmentActionBar(
                canMarkComplete = canMarkComplete,
                canCancel = canCancel,
                onRecordPayment = onRecordPayment,
                onMarkComplete = {
                    incompleteTopics = emptyList()
                    showMarkComplete = true
                },
                onCancel = { showCancel = true }
            )
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
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BaseWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 12.dp)
        )
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
        EnrollmentStatus.Ongoing -> StatusEmerald
        EnrollmentStatus.Completed -> StatusGray
        EnrollmentStatus.Cancelled -> StatusRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredPhotoAvatar(
                    name = enrollment.studentName ?: enrollment.courseName ?: "?",
                    size = 56
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                        color = OnSurfaceVariantLightColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(statusLabel, statusColor, fontSize = 10.sp)
            }

            BillingTypePills(selected = enrollment.billingType)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryField("Start", Formatters.formatDateIst(enrollment.startDate), Modifier.weight(1f))
                    SummaryField(
                        "End / Expected",
                        Formatters.formatDateIst(enrollment.expectedEndDate),
                        Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryField("Effective Fee", Formatters.formatInr(enrollment.effectiveFee), Modifier.weight(1f))
                    SummaryField(
                        "Enrollment Fee",
                        Formatters.formatInr(enrollment.effectiveEnrollmentFee),
                        Modifier.weight(1f)
                    )
                }
            }

            ProgressRow(label = "Fee paid", percent = feePercent, color = BrandBlue)
            if (enrollment.topicsSummary?.hasTopics == true || !enrollment.topics.isNullOrEmpty()) {
                ProgressRow(label = "Topics completed", percent = topicsPercent, color = BrandRed)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BillingTypePills(selected: BillingType?) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BillingTypePill("Installment", selected == BillingType.Installment)
        BillingTypePill("Monthly", selected == BillingType.Subscription)
    }
}

@Composable
private fun BillingTypePill(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (active) BrandBlueTint else BaseWhite)
            .border(
                width = 1.dp,
                color = if (active) BrandBlue.copy(alpha = 0.35f) else OutlineVariantLight,
                shape = PillShape
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) BrandBlue else OnSurfaceVariantLightColor
        )
    }
}

@Composable
private fun SummaryField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantLightColor)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProgressRow(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
            Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
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
    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Installment Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (canEdit) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Edit", color = BrandBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        installments.forEachIndexed { index, installment ->
            if (index > 0) Spacer(Modifier.height(8.dp))
            InstallmentScheduleRow(installment = installment, onPay = onPay)
        }
    }
}

@Composable
private fun InstallmentScheduleRow(installment: Installment, onPay: () -> Unit) {
    val rowStyle = when (installment.status) {
        InstallmentStatus.Paid -> InstallmentRowStyle(PaidRowTint, "Paid", StatusEmerald, false)
        InstallmentStatus.Overdue -> InstallmentRowStyle(OverdueRowTint, "Overdue", StatusRed, true)
        InstallmentStatus.Partial -> InstallmentRowStyle(PendingRowTint, "Partial", BackdateOrange, true)
        InstallmentStatus.Unpaid -> InstallmentRowStyle(PendingRowTint, "Pending", BackdateOrange, true)
    }
    val bg = rowStyle.background
    val statusLabel = rowStyle.statusLabel
    val statusColor = rowStyle.statusColor
    val showPay = rowStyle.showPay

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                color = OnSurfaceVariantLightColor
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Pill(statusLabel, statusColor, fontSize = 10.sp)
            if (showPay) {
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = BaseWhite),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Text("Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    val percent = summary?.progressPercent
        ?: if (topics.isNotEmpty()) topics.count { it.isCompleted } * 100 / topics.size else 0

    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Topics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("$percent%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color = BrandBlue,
            trackColor = BrandBlue.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(12.dp))
        topics.forEachIndexed { index, topic ->
            if (index > 0) {
                HorizontalDivider(color = OutlineVariantLight, modifier = Modifier.padding(vertical = 10.dp))
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (topic.isCompleted) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (topic.isCompleted) StatusEmerald else OnSurfaceVariantLightColor,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                topic.topicName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            topic.description?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
            }
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(OffWhite)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(durationLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantLightColor)
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
                    color = StatusEmerald,
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
    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Payment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        payments.forEachIndexed { index, payment ->
            if (index > 0) {
                HorizontalDivider(color = OutlineVariantLight, modifier = Modifier.padding(vertical = 10.dp))
            }
            PaymentHistoryRow(payment = payment, onReceipt = { onReceipt(payment) })
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CurrencyText(payment.amount, style = MaterialTheme.typography.titleMedium, bold = true)
            Text(
                "${Formatters.formatDateIst(payment.paymentDate)} · $methodLabel",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantLightColor
            )
            Pill(
                text = if (isVoided) "Voided" else "Paid",
                color = if (isVoided) StatusRed else StatusEmerald,
                fontSize = 10.sp
            )
        }
        if (!payment.receiptId.isNullOrBlank()) {
            TextButton(onClick = onReceipt) {
                Text("Receipt", color = BrandBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BillingExclusionCard(excluded: Boolean, onToggle: (Boolean) -> Unit) {
    DetailCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Exclude from billing tracking",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "This enrollment won't appear in pending reports",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLightColor
                )
            }
            Switch(
                checked = excluded,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BaseWhite,
                    checkedTrackColor = BrandRed
                )
            )
        }
    }
}

@Composable
private fun EnrollmentActionBar(
    canMarkComplete: Boolean,
    canCancel: Boolean,
    onRecordPayment: () -> Unit,
    onMarkComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BaseWhite)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onRecordPayment,
            modifier = Modifier.fillMaxWidth(),
            shape = FieldShape,
            colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = BaseWhite)
        ) {
            Icon(Icons.Outlined.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Record Payment", fontWeight = FontWeight.Bold)
        }
        if (canMarkComplete) {
            OutlinedButton(
                onClick = onMarkComplete,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BrandBlueTint,
                    contentColor = BrandBlue
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue.copy(alpha = 0.35f))
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Mark Complete", fontWeight = FontWeight.SemiBold)
            }
        }
        if (canCancel) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Cancel Enrollment", fontWeight = FontWeight.SemiBold)
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
