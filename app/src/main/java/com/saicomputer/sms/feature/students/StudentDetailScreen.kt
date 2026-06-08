package com.saicomputer.sms.feature.students

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.CrossfadeUiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.StatusBadge
import com.saicomputer.sms.core.ui.StudentPhotoAvatar
import com.saicomputer.sms.core.ui.rememberBase64ImageBitmap
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
import com.saicomputer.sms.data.model.Gender
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.PaymentMethod
import com.saicomputer.sms.data.model.PaymentStatus
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.TERMINAL_STUDENT_STATUSES
import kotlinx.coroutines.CoroutineScope

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)

private val GENDER_LABELS = mapOf(
    Gender.Male to "Male",
    Gender.Female to "Female",
    Gender.Other to "Other",
    Gender.PreferNotToSay to "Prefer not to say"
)

@OptIn(ExperimentalLayoutApi::class)
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

    CrossfadeUiState(
        state = state,
        loading = {
            Column(Modifier.fillMaxSize().background(OffWhite)) {
                StudentDetailHeader(title = "Student", onBack = onBack, onEdit = null)
                LoadingSkeleton(modifier = Modifier.fillMaxSize())
            }
        },
        error = { message ->
            Column(Modifier.fillMaxSize().background(OffWhite)) {
                StudentDetailHeader(title = "Student", onBack = onBack, onEdit = null)
                ErrorState(message = message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize())
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
                viewModel = viewModel,
                snackbarController = snackbarController,
                scope = scope
            )
        }
    )
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
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewEnrollment: () -> Unit,
    onOpenEnrollment: (String) -> Unit,
    onRecordPayment: (String) -> Unit,
    documentsViewModel: StudentDocumentsViewModel,
    viewModel: StudentDetailViewModel,
    snackbarController: SnackbarController,
    scope: CoroutineScope
) {
    var statusDialog by remember { mutableStateOf<StatusDialogMode?>(null) }
    var showAadhaar by remember { mutableStateOf(false) }
    var voidPaymentTarget by remember { mutableStateOf<Payment?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Profile", "Enrollments", "Payments", "Documents")
    val isTerminal = student.status in TERMINAL_STUDENT_STATUSES
    val hasEnrollments = enrollments.isNotEmpty()
    val ongoingEnrollment = enrollments.firstOrNull { it.enrollmentStatus == EnrollmentStatus.Ongoing }
    val photoState by documentsViewModel.photo.collectAsStateWithLifecycle()
    val documentsBusy by documentsViewModel.busy.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            documentsViewModel.replacePhoto(
                student.studentId,
                uri,
                StudentFormViewModel.PHOTO_MAX_BYTES
            ) { ok, msg ->
                snackbarController.show(scope, msg)
                if (ok) {
                    documentsViewModel.clearPhoto()
                    documentsViewModel.loadPhoto(student.studentId)
                }
            }
        }
    }

    DisposableEffect(student.studentId) {
        documentsViewModel.loadPhoto(student.studentId)
        onDispose { documentsViewModel.clearPhoto() }
    }

    Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        StudentDetailHeader(
            title = student.fullName,
            onBack = onBack,
            onEdit = if (canEdit) onEdit else null
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            StudentHeroCard(
                student = student,
                photoBase64 = photoState.file?.base64,
                photoLoading = photoState.loading
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = BaseWhite,
                contentColor = BrandBlue,
                edgePadding = 16.dp,
                divider = {},
                indicator = { positions ->
                    if (selectedTab < positions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                            height = 3.dp,
                            color = BrandRed
                        )
                    }
                }
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
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == index) BrandRed else OnSurfaceVariantLightColor
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> ProfileTab(student)
                    1 -> EnrollmentsTab(enrollments, onOpenEnrollment)
                    2 -> PaymentsTab(
                        payments = payments,
                        canVoid = canVoid,
                        onViewReceipt = { payment ->
                            val receiptId = payment.receiptId
                            if (receiptId.isNullOrBlank()) {
                                snackbarController.show(scope, "No receipt for this payment")
                            } else {
                                viewModel.loadReceipt(
                                    receiptId,
                                    onSuccess = { receipt ->
                                        val url = receipt.previewUrl ?: receipt.downloadUrl
                                        if (url != null) {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } else {
                                            snackbarController.show(scope, "Receipt ${receipt.receiptId}")
                                        }
                                    },
                                    onError = { snackbarController.show(scope, it) }
                                )
                            }
                        },
                        onVoid = { voidPaymentTarget = it }
                    )
                    3 -> DocumentsTab(
                        student = student,
                        photoBase64 = photoState.file?.base64,
                        photoLoading = photoState.loading,
                        busy = documentsBusy,
                        onReplacePhoto = { photoPicker.launch("image/*") },
                        onViewAadhaar = { showAadhaar = true }
                    )
                }
            }
        }

        StudentActionBar(
            canEdit = canEdit,
            canChangeStatus = canChangeStatus,
            isTerminal = isTerminal,
            hasEnrollments = hasEnrollments,
            onNewEnrollment = onNewEnrollment,
            onRecordPayment = {
                val enrollmentId = ongoingEnrollment?.enrollmentId
                if (enrollmentId != null) {
                    onRecordPayment(enrollmentId)
                } else {
                    snackbarController.show(scope, "No active enrollment to record payment against")
                }
            },
            onDropout = { statusDialog = StatusDialogMode.Dropout },
            onNotTakenAdmission = { statusDialog = StatusDialogMode.NotTakenAdmission },
            onReactivate = { statusDialog = StatusDialogMode.Reactivate }
        )
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

    voidPaymentTarget?.let { payment ->
        VoidPaymentDialog(
            onConfirm = { reason ->
                voidPaymentTarget = null
                viewModel.voidPayment(
                    paymentId = payment.paymentId,
                    reason = reason,
                    onSuccess = { snackbarController.show(scope, "Payment voided") },
                    onError = { snackbarController.show(scope, it) }
                )
            },
            onDismiss = { voidPaymentTarget = null }
        )
    }

    if (showAadhaar) {
        AadhaarViewerDialog(
            studentId = student.studentId,
            maskedNumber = student.aadhaarNumber,
            canReplace = canReplaceAadhaar,
            maxBytes = StudentFormViewModel.AADHAAR_MAX_BYTES,
            onDismiss = { showAadhaar = false },
            onReplaced = { snackbarController.show(scope, it) },
            viewModel = documentsViewModel
        )
    }
}

@Composable
private fun StudentDetailHeader(
    title: String,
    onBack: () -> Unit,
    onEdit: (() -> Unit)?
) {
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit student", tint = BaseWhite)
            }
        }
    }
}

@Composable
private fun StudentHeroCard(
    student: Student,
    photoBase64: String?,
    photoLoading: Boolean
) {
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentPhotoAvatar(
                name = student.fullName,
                base64 = photoBase64,
                loading = photoLoading,
                size = 64
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        student.studentId,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandBlue
                    )
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "Copy ID",
                        tint = BrandBlue,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                clipboard.setText(AnnotatedString(student.studentId))
                            }
                    )
                }
                StatusBadge(status = student.status, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ProfileTab(student: Student) {
    DetailCard {
        ProfileSection("BASIC INFO") {
            DetailField("Full Name", student.fullName)
            DetailField("Phone", Formatters.formatPhone(student.phoneNumber))
            student.email?.let { DetailField("Email", it) }
            student.dateOfBirth?.let { DetailField("Date of Birth", Formatters.formatDateIst(it)) }
            student.gender?.let { DetailField("Gender", GENDER_LABELS[it] ?: it.name) }
            student.address?.let { DetailField("Address", it) }
        }

        ProfileSection("PARENT / GUARDIAN") {
            student.parentName?.let { DetailField("Parent Name", it) }
            student.parentPhone?.let { DetailField("Parent Phone", Formatters.formatPhone(it)) }
        }

        ProfileSection("ACADEMIC") {
            student.academicQualification?.let { DetailField("Qualification", it) }
            student.lastInstitution?.let { DetailField("Last Institution", it) }
            student.additionalNotes?.let { DetailField("Notes", it) }
        }

        if (student.registrationSession != RegistrationSession.NewRecord) {
            ProfileSection("LEGACY REGISTRATION") {
                DetailField("Session", student.registrationSession.name)
                student.oldRegistrationNumber?.let { DetailField("Old Registration No.", it) }
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariantLightColor,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
        )
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(0.dp), content = content)
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariantLightColor
        )
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = OutlineVariantLight)
    }
}

@Composable
private fun EnrollmentsTab(enrollments: List<Enrollment>, onOpen: (String) -> Unit) {
    if (enrollments.isEmpty()) {
        EmptyTabMessage("No enrollments yet.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        enrollments.forEach { enrollment ->
            EnrollmentCard(enrollment = enrollment, onClick = { onOpen(enrollment.enrollmentId) })
        }
    }
}

@Composable
private fun EnrollmentCard(enrollment: Enrollment, onClick: () -> Unit) {
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
    val feePercent = if (enrollment.totalAmountDue > 0) {
        (enrollment.totalAmountPaid * 100 / enrollment.totalAmountDue).coerceIn(0, 100)
    } else 0
    val topicsPercent = enrollment.topicsSummary?.progressPercent ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    enrollment.courseName ?: enrollment.courseId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Pill(statusLabel, statusColor, fontSize = 10.sp)
            }
            Text(
                "${Formatters.formatDateIst(enrollment.startDate)} → ${Formatters.formatDateIst(enrollment.expectedEndDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantLightColor
            )
            enrollment.billingType?.let { billing ->
                Pill(
                    text = when (billing) {
                        BillingType.Installment -> "Installment"
                        BillingType.Subscription -> "Subscription"
                    },
                    color = BrandBlue,
                    fontSize = 10.sp
                )
            }
            FeeProgressBar(label = "Fee paid", percent = feePercent, color = BrandBlue)
            if (enrollment.topicsSummary?.hasTopics == true) {
                FeeProgressBar(label = "Topics", percent = topicsPercent, color = BrandRed)
            }
        }
    }
}

@Composable
private fun FeeProgressBar(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
            Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun PaymentsTab(
    payments: List<Payment>,
    canVoid: Boolean,
    onViewReceipt: (Payment) -> Unit,
    onVoid: (Payment) -> Unit
) {
    if (payments.isEmpty()) {
        EmptyTabMessage("No payments recorded yet.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        payments.forEach { payment ->
            PaymentCard(
                payment = payment,
                canVoid = canVoid,
                onViewReceipt = { onViewReceipt(payment) },
                onVoid = { onVoid(payment) }
            )
        }
    }
}

@Composable
private fun PaymentCard(
    payment: Payment,
    canVoid: Boolean,
    onViewReceipt: () -> Unit,
    onVoid: () -> Unit
) {
    val isVoided = payment.status == PaymentStatus.Voided
    val methodLabel = when (payment.paymentMethod) {
        PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
        else -> payment.paymentMethod.name
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurrencyText(payment.amount, style = MaterialTheme.typography.titleMedium, bold = true)
                Pill(
                    text = if (isVoided) "Voided" else "Paid",
                    color = if (isVoided) StatusRed else StatusEmerald,
                    fontSize = 10.sp
                )
            }
            Text(
                "${Formatters.formatDateIst(payment.paymentDate)} · $methodLabel",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantLightColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!payment.receiptId.isNullOrBlank()) {
                    TextButton(onClick = onViewReceipt) {
                        Text("View Receipt", color = BrandBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (canVoid && !isVoided) {
                    TextButton(onClick = onVoid) {
                        Text("Void", color = BrandRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentsTab(
    student: Student,
    photoBase64: String?,
    photoLoading: Boolean,
    busy: Boolean,
    onReplacePhoto: () -> Unit,
    onViewAadhaar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = BaseWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .dashedBorder(OutlineVariantLight)
                        .background(OffWhite, FieldShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        photoLoading -> CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        photoBase64 != null -> {
                            val bitmap = rememberBase64ImageBitmap(photoBase64)
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap,
                                    contentDescription = "Student photo",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            } else {
                                ColoredPhotoAvatar(name = student.fullName, size = 96)
                            }
                        }
                        else -> ColoredPhotoAvatar(name = student.fullName, size = 96)
                    }
                }
                OutlinedButton(
                    onClick = onReplacePhoto,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(if (busy) "Uploading…" else "Replace Photo")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = BaseWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Aadhaar Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Masked: ${Formatters.maskAadhaar(student.aadhaarNumber).ifBlank { "Not set" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLightColor
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandBlueTint, FieldShape)
                        .border(1.dp, BrandBlue.copy(alpha = 0.2f), FieldShape)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    Text(
                        "Viewing this document will be logged for audit purposes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandBlue
                    )
                }
                OutlinedButton(
                    onClick = onViewAadhaar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("View Aadhaar")
                }
            }
        }
    }
}

@Composable
private fun StudentActionBar(
    canEdit: Boolean,
    canChangeStatus: Boolean,
    isTerminal: Boolean,
    hasEnrollments: Boolean,
    onNewEnrollment: () -> Unit,
    onRecordPayment: () -> Unit,
    onDropout: () -> Unit,
    onNotTakenAdmission: () -> Unit,
    onReactivate: () -> Unit
) {
    if (!canEdit && !canChangeStatus) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BaseWhite)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (canEdit) {
            Button(
                onClick = onNewEnrollment,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = BaseWhite)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("New Enrollment", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onRecordPayment,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BrandBlueTint,
                    contentColor = BrandBlue
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue.copy(alpha = 0.35f))
            ) {
                Icon(Icons.Outlined.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Record Payment", fontWeight = FontWeight.SemiBold)
            }
        }
        if (canChangeStatus) {
            if (!isTerminal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDropout,
                        enabled = hasEnrollments,
                        modifier = Modifier.weight(1f),
                        shape = FieldShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Outlined.PersonOff, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Dropout", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onNotTakenAdmission,
                        enabled = !hasEnrollments,
                        modifier = Modifier.weight(1f),
                        shape = FieldShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OnSurfaceVariantLightColor,
                            disabledContentColor = OnSurfaceVariantLightColor.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!hasEnrollments) BrandRed.copy(alpha = 0.4f) else OutlineVariantLight
                        )
                    ) {
                        Icon(Icons.Outlined.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Not Admitted", fontSize = 13.sp)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onReactivate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape
                ) {
                    Text("Reactivate Student", fontWeight = FontWeight.SemiBold)
                }
            }
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
                Spacer(Modifier.height(8.dp))
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
            ) { Text("Void", color = BrandRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmptyTabMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite)
    ) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariantLightColor
        )
    }
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
