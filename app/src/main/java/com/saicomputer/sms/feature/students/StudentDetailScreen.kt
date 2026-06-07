package com.saicomputer.sms.feature.students

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.CrossfadeUiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.DateText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.RegistrationSessionBadge
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.StatusBadge
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import com.saicomputer.sms.data.model.TERMINAL_STUDENT_STATUSES
import androidx.compose.runtime.rememberCoroutineScope
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

    androidx.compose.runtime.LaunchedEffect(studentId) { viewModel.load(studentId) }

    Scaffold(
        topBar = { SmsTopBar(title = "Student", onBack = onBack) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            CrossfadeUiState(
                state = state,
                loading = { LoadingSkeleton(modifier = Modifier.fillMaxSize()) },
                error = { message -> ErrorState(message = message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize()) },
                success = { data ->
                    StudentDetailContent(
                        student = data.student,
                        enrollments = data.enrollments.orEmpty(),
                        payments = data.payments.orEmpty(),
                        canChangeStatus = can(user, "students.changeStatus"),
                        canEdit = true,
                        canVoid = can(user, "payments.void"),
                        canReplaceAadhaar = can(user, "students.replaceAadhaar"),
                        onEdit = onEdit,
                        onNewEnrollment = onNewEnrollment,
                        onOpenEnrollment = onOpenEnrollment,
                        onRecordPayment = onRecordPayment,
                        documentsViewModel = documentsViewModel,
                        onChangeStatus = { mode ->
                            // handled inside content via dialog
                        },
                        viewModel = viewModel,
                        snackbarController = snackbarController,
                        scope = scope
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentDetailContent(
    student: Student,
    enrollments: List<Enrollment>,
    payments: List<Payment>,
    canChangeStatus: Boolean,
    canEdit: Boolean,
    canVoid: Boolean,
    canReplaceAadhaar: Boolean,
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
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Profile", "Enrollments", "Payments", "Documents")
    val isTerminal = student.status in TERMINAL_STUDENT_STATUSES
    val hasEnrollments = enrollments.isNotEmpty()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(student.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(student.studentId, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = student.status)
                RegistrationSessionBadge(session = student.registrationSession)
                if (student.isBackdate) GenericBadge("Backdated", StatusAmber)
                if (enrollments.any { it.excludedFromBilling }) GenericBadge("Billing excluded", StatusAmber)
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            when (selectedTab) {
                0 -> ProfileTab(student)
                1 -> EnrollmentsTab(enrollments, onOpenEnrollment)
                2 -> PaymentsTab(payments)
                3 -> DocumentsTab(
                    onViewPhoto = { showPhoto = true },
                    onViewAadhaar = { showAadhaar = true }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Actions
            if (canEdit) {
                OutlinedButton(onClick = onNewEnrollment, modifier = Modifier.fillMaxWidth()) {
                    Text("New Enrollment for this student")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit Student")
                }
            }
            if (canChangeStatus) {
                Spacer(Modifier.height(8.dp))
                if (!isTerminal) {
                    OutlinedButton(
                        onClick = { statusDialog = StatusDialogMode.Dropout },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Mark as Dropout", color = StatusRed) }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { statusDialog = StatusDialogMode.NotTakenAdmission },
                        enabled = !hasEnrollments,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Mark as Not Taken Admission") }
                } else {
                    OutlinedButton(
                        onClick = { statusDialog = StatusDialogMode.Reactivate },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Reactivate Student") }
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

@Composable
private fun ProfileTab(student: Student) {
    Column {
        InfoRow("Phone", com.saicomputer.sms.core.format.Formatters.formatPhone(student.phoneNumber))
        student.email?.let { InfoRow("Email", it) }
        student.parentName?.let { InfoRow("Parent", it) }
        student.parentPhone?.let { InfoRow("Parent phone", com.saicomputer.sms.core.format.Formatters.formatPhone(it)) }
        student.dateOfBirth?.let { InfoRow("Date of birth", com.saicomputer.sms.core.format.Formatters.formatDateIst(it)) }
        student.gender?.let { InfoRow("Gender", it.name) }
        student.address?.let { InfoRow("Address", it) }
        student.academicQualification?.let { InfoRow("Qualification", it) }
        student.lastInstitution?.let { InfoRow("Last institution", it) }
        student.aadhaarNumber?.let {
            InfoRow("Aadhaar", com.saicomputer.sms.core.format.Formatters.maskAadhaar(it))
        }
        student.additionalNotes?.let { InfoRow("Notes", it) }
        if (student.registrationSession != RegistrationSession.NewRecord) {
            Spacer(Modifier.height(12.dp))
            Text("Legacy Registration", fontWeight = FontWeight.SemiBold)
            InfoRow("Session", student.registrationSession.name)
            student.oldRegistrationNumber?.let { InfoRow("Old Registration No.", it) }
        }
    }
}

@Composable
private fun EnrollmentsTab(enrollments: List<Enrollment>, onOpen: (String) -> Unit) {
    if (enrollments.isEmpty()) {
        Text("No enrollments yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        enrollments.forEach { e ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(e.enrollmentId) }) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(e.courseName ?: e.courseId, fontWeight = FontWeight.SemiBold)
                        GenericBadge(e.enrollmentStatus.name)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Paid ", style = MaterialTheme.typography.bodyMedium)
                        CurrencyText(e.totalAmountPaid, style = MaterialTheme.typography.bodyMedium)
                        Text(" / ", style = MaterialTheme.typography.bodyMedium)
                        CurrencyText(e.totalAmountDue, style = MaterialTheme.typography.bodyMedium)
                    }
                    e.billingType?.let { GenericBadge(it.name) }
                }
            }
        }
    }
}

@Composable
private fun PaymentsTab(payments: List<Payment>) {
    if (payments.isEmpty()) {
        Text("No payments yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        payments.forEach { p ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        CurrencyText(p.amount, style = MaterialTheme.typography.bodyLarge, bold = true)
                        DateText(p.paymentDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    GenericBadge(p.paymentMethod.name)
                }
            }
        }
    }
}

@Composable
private fun DocumentsTab(onViewPhoto: () -> Unit, onViewAadhaar: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onViewPhoto, modifier = Modifier.fillMaxWidth()) { Text("View Photo") }
        OutlinedButton(onClick = onViewAadhaar, modifier = Modifier.fillMaxWidth()) { Text("View Aadhaar (logged)") }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            modifier = Modifier.weight(0.4f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(value, modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium)
    }
}
