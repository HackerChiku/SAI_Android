package com.saicomputer.sms.feature.students

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CrossfadeUiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.DateText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.ListItemCard
import com.saicomputer.sms.core.ui.ListItemIconBox
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.StudentPhotoAvatar
import com.saicomputer.sms.core.ui.RegistrationSessionBadge
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.StatusBadge
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandGold
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.PaymentStatus
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.TERMINAL_STUDENT_STATUSES
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentDetailScreen(
    studentId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewEnrollment: () -> Unit,
    onOpenEnrollment: (String) -> Unit,
    onRecordPayment: (String) -> Unit,
    snackbarController: SnackbarController,
    viewModel: StudentDetailViewModel = hiltViewModel(),
    documentsViewModel: StudentDocumentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val topBarTitle = when (val s = state) {
        is UiState.Success -> s.data.student.fullName
        else -> "Student"
    }

    androidx.compose.runtime.LaunchedEffect(studentId) { viewModel.load(studentId) }

    CrossfadeUiState(
        state = state,
        loading = {
            Scaffold(topBar = { SmsTopBar(title = topBarTitle, onBack = onBack) }) { padding ->
                LoadingSkeleton(modifier = Modifier.fillMaxSize().padding(padding))
            }
        },
        error = { message ->
            Scaffold(topBar = { SmsTopBar(title = topBarTitle, onBack = onBack) }) { padding ->
                ErrorState(
                    message = message,
                    onRetry = viewModel::reload,
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
        },
        success = { data ->
            StudentDetailContent(
                student = data.student,
                enrollments = data.enrollments.orEmpty(),
                payments = data.payments.orEmpty(),
                canChangeStatus = can(user, "students.changeStatus"),
                canEdit = true,
                canVoid = can(user, "payments.void"),
                canReplaceAadhaar = can(user, "students.replaceAadhaar"),
                onBack = onBack,
                onEdit = onEdit,
                onNewEnrollment = onNewEnrollment,
                onOpenEnrollment = onOpenEnrollment,
                onRecordPayment = onRecordPayment,
                documentsViewModel = documentsViewModel,
                onChangeStatus = { },
                viewModel = viewModel,
                snackbarController = snackbarController,
                scope = scope
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun StudentDetailContent(
    student: Student,
    enrollments: List<Enrollment>,
    payments: List<Payment>,
    canChangeStatus: Boolean,
    canEdit: Boolean,
    canVoid: Boolean,
    canReplaceAadhaar: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewEnrollment: () -> Unit,
    onOpenEnrollment: (String) -> Unit,
    onRecordPayment: (String) -> Unit,
    documentsViewModel: StudentDocumentsViewModel,
    onChangeStatus: (StatusDialogMode) -> Unit,
    viewModel: StudentDetailViewModel,
    snackbarController: SnackbarController,
    scope: CoroutineScope
) {
    var statusDialog by remember { mutableStateOf<StatusDialogMode?>(null) }
    var showPhoto by remember { mutableStateOf(false) }
    var showAadhaar by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val actionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tabs = listOf("Profile", "Enrollments", "Payments", "Documents")
    val isTerminal = student.status in TERMINAL_STUDENT_STATUSES
    val hasEnrollments = enrollments.isNotEmpty()
    val activePayments = payments.filter { it.status == PaymentStatus.Active }
    val totalPaid = activePayments.sumOf { it.amount }
    val showFab = canEdit || canChangeStatus
    val photoState by documentsViewModel.photo.collectAsStateWithLifecycle()

    DisposableEffect(student.studentId) {
        documentsViewModel.loadPhoto(student.studentId)
        onDispose { documentsViewModel.clearPhoto() }
    }

    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = actionSheetState
        ) {
            StudentActionsSheet(
                canEdit = canEdit,
                canChangeStatus = canChangeStatus,
                isTerminal = isTerminal,
                hasEnrollments = hasEnrollments,
                onNewEnrollment = {
                    showActions = false
                    onNewEnrollment()
                },
                onEdit = {
                    showActions = false
                    onEdit()
                },
                onDropout = {
                    showActions = false
                    statusDialog = StatusDialogMode.Dropout
                },
                onNotTakenAdmission = {
                    showActions = false
                    statusDialog = StatusDialogMode.NotTakenAdmission
                },
                onReactivate = {
                    showActions = false
                    statusDialog = StatusDialogMode.Reactivate
                }
            )
        }
    }

    Scaffold(
        topBar = { SmsTopBar(title = student.fullName, onBack = onBack) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = { showActions = true }) {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "Student actions")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            StudentHeroHeader(
                student = student,
                photoBase64 = photoState.file?.base64,
                photoLoading = photoState.loading,
                enrollmentCount = enrollments.size,
                paymentCount = activePayments.size,
                totalPaid = totalPaid,
                hasBillingExcluded = enrollments.any { it.excludedFromBilling }
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> ProfileTab(student)
                    1 -> EnrollmentsTab(enrollments, onOpenEnrollment)
                    2 -> PaymentsTab(payments)
                    3 -> DocumentsTab(
                        onViewPhoto = { showPhoto = true },
                        onViewAadhaar = { showAadhaar = true }
                    )
                }
            }
        }
    }

    statusDialog?.let { mode ->
        ChangeStudentStatusDialog(
            mode = mode,
            enrollments = enrollments,
            onConfirm = { status, reason ->
                statusDialog = null
                viewModel.changeStatus(
                    newStatus = status,
                    reason = reason,
                    onResult = { res ->
                        val cancelled = res.cascadeCancelledEnrollments.size
                        val msg = buildString {
                            append("Status updated to ${res.newStatus}.")
                            if (cancelled > 0) append(" $cancelled enrollment(s) cancelled.")
                        }
                        snackbarController.show(scope, msg)
                    },
                    onError = { snackbarController.show(scope, it) }
                )
            },
            onDismiss = { statusDialog = null }
        )
    }

    if (showPhoto) {
        PhotoViewerDialog(
            studentId = student.studentId,
            canReplace = true,
            maxBytes = 2L * 1024 * 1024,
            onDismiss = { showPhoto = false },
            onReplaced = { snackbarController.show(scope, it) },
            viewModel = documentsViewModel
        )
    }
    if (showAadhaar) {
        AadhaarViewerDialog(
            studentId = student.studentId,
            maskedNumber = student.aadhaarNumber,
            canReplace = canReplaceAadhaar,
            maxBytes = 5L * 1024 * 1024,
            onDismiss = { showAadhaar = false },
            onReplaced = { snackbarController.show(scope, it) },
            viewModel = documentsViewModel
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentHeroHeader(
    student: Student,
    photoBase64: String?,
    photoLoading: Boolean,
    enrollmentCount: Int,
    paymentCount: Int,
    totalPaid: Int,
    hasBillingExcluded: Boolean
) {
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
                StudentPhotoAvatar(
                    name = student.fullName,
                    base64 = photoBase64,
                    loading = photoLoading,
                    size = 88
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            student.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            student.studentId,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            Formatters.formatPhone(student.phoneNumber),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(status = student.status)
                RegistrationSessionBadge(session = student.registrationSession)
                if (student.isBackdate) GenericBadge("Backdated", StatusAmber)
                if (hasBillingExcluded) GenericBadge("Billing excluded", StatusAmber)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Enrollments", value = enrollmentCount.toString())
                VerticalStatDivider()
                StatItem(label = "Payments", value = paymentCount.toString())
                VerticalStatDivider()
                StatItem(
                    label = "Total paid",
                    value = Formatters.formatInr(totalPaid),
                    emphasized = true
                )
            }
        }
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
private fun StudentActionsSheet(
    canEdit: Boolean,
    canChangeStatus: Boolean,
    isTerminal: Boolean,
    hasEnrollments: Boolean,
    onNewEnrollment: () -> Unit,
    onEdit: () -> Unit,
    onDropout: () -> Unit,
    onNotTakenAdmission: () -> Unit,
    onReactivate: () -> Unit
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
        if (canEdit) {
            StudentActionItem(
                label = "New Enrollment",
                icon = Icons.Outlined.Add,
                onClick = onNewEnrollment
            )
            StudentActionItem(
                label = "Edit Student",
                icon = Icons.Outlined.Edit,
                onClick = onEdit
            )
        }
        if (canChangeStatus && !isTerminal) {
            if (hasEnrollments) {
                StudentActionItem(
                    label = "Mark as Dropout",
                    icon = Icons.Outlined.PersonOff,
                    destructive = true,
                    onClick = onDropout
                )
            } else {
                StudentActionItem(
                    label = "Not Taken Admission",
                    icon = Icons.Outlined.EventBusy,
                    onClick = onNotTakenAdmission
                )
            }
        } else if (canChangeStatus && isTerminal) {
            StudentActionItem(
                label = "Reactivate Student",
                icon = Icons.Outlined.RestartAlt,
                onClick = onReactivate
            )
        }
    }
}

@Composable
private fun StudentActionItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@Composable
private fun StatItem(label: String, value: String, emphasized: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileTab(student: Student) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSection("Contact") {
            DetailInfoRow("Phone", Formatters.formatPhone(student.phoneNumber))
            student.email?.let { DetailInfoRow("Email", it) }
            student.address?.let { DetailInfoRow("Address", it) }
        }

        DetailSection("Personal") {
            student.dateOfBirth?.let {
                DetailInfoRow("Date of birth", Formatters.formatDateIst(it))
            }
            student.gender?.let { DetailInfoRow("Gender", it.name) }
            student.parentName?.let { DetailInfoRow("Parent", it) }
            student.parentPhone?.let {
                DetailInfoRow("Parent phone", Formatters.formatPhone(it))
            }
        }

        DetailSection("Academic") {
            student.academicQualification?.let { DetailInfoRow("Qualification", it) }
            student.lastInstitution?.let { DetailInfoRow("Last institution", it) }
        }

        DetailSection("Identity & notes") {
            student.aadhaarNumber?.let {
                DetailInfoRow("Aadhaar", Formatters.maskAadhaar(it))
            }
            student.additionalNotes?.let { DetailInfoRow("Notes", it) }
        }

        if (student.registrationSession != RegistrationSession.NewRecord) {
            DetailSection("Legacy registration") {
                DetailInfoRow("Session", student.registrationSession.name)
                student.oldRegistrationNumber?.let {
                    DetailInfoRow("Old registration no.", it)
                }
            }
        }
    }
}

@Composable
private fun EnrollmentsTab(enrollments: List<Enrollment>, onOpen: (String) -> Unit) {
    if (enrollments.isEmpty()) {
        EmptyTabMessage("No enrollments yet. Tap New Enrollment to add one.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        enrollments.forEach { enrollment ->
            EnrollmentRow(enrollment = enrollment, onClick = { onOpen(enrollment.enrollmentId) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EnrollmentRow(enrollment: Enrollment, onClick: () -> Unit) {
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

    ListItemCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            ListItemIconBox(icon = Icons.AutoMirrored.Outlined.MenuBook)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        enrollment.courseName ?: enrollment.courseId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                enrollment.courseFullName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Paid ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyText(enrollment.totalAmountPaid, style = MaterialTheme.typography.bodySmall, bold = true)
                    Text(
                        " / ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyText(
                        enrollment.totalAmountDue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GenericBadge(enrollment.enrollmentStatus.name, statusColor)
                    enrollment.billingType?.let { GenericBadge(it.name, billingColor) }
                    if (enrollment.excludedFromBilling) GenericBadge("Billing excluded", StatusAmber)
                }
            }
        }
    }
}

@Composable
private fun PaymentsTab(payments: List<Payment>) {
    if (payments.isEmpty()) {
        EmptyTabMessage("No payments recorded yet.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        payments.forEach { payment ->
            PaymentRow(payment = payment)
        }
    }
}

@Composable
private fun PaymentRow(payment: Payment) {
    val isVoided = payment.status == PaymentStatus.Voided

    ListItemCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListItemIconBox(icon = Icons.Outlined.Payments)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CurrencyText(
                    payment.amount,
                    style = MaterialTheme.typography.titleMedium,
                    bold = true
                )
                DateText(
                    payment.paymentDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                payment.receiptId?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                GenericBadge(payment.paymentMethod.name, BrandBlue)
                if (isVoided) GenericBadge("Voided", StatusRed)
            }
        }
    }
}

@Composable
private fun DocumentsTab(onViewPhoto: () -> Unit, onViewAadhaar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DocumentTile(
            title = "Student photo",
            subtitle = "View or replace profile photo",
            icon = Icons.Outlined.Face,
            onClick = onViewPhoto
        )
        DocumentTile(
            title = "Aadhaar document",
            subtitle = "View logged access only",
            icon = Icons.Outlined.Badge,
            onClick = onViewAadhaar
        )
    }
}

@Composable
private fun DocumentTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItemCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListItemIconBox(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ListItemCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = sectionIcon(title),
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

private fun sectionIcon(title: String) = when (title) {
    "Contact" -> Icons.Outlined.Phone
    "Personal" -> Icons.Outlined.Person
    "Academic" -> Icons.Outlined.School
    else -> Icons.Outlined.Badge
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
private fun EmptyTabMessage(message: String) {
    ListItemCard {
        Text(
            message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
