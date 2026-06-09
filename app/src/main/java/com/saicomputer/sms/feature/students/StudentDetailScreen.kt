package com.saicomputer.sms.feature.students

import com.saicomputer.sms.core.ui.studentStatusColor
import com.saicomputer.sms.core.ui.theme.appColors
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.CrossfadeUiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.ThemedShimmerCircle
import com.saicomputer.sms.core.ui.PaymentActionButtons
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.StudentPhotoAvatar
import com.saicomputer.sms.core.ui.rememberBase64ImageBitmap
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus
import com.saicomputer.sms.data.model.Gender
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.PaymentMethod
import com.saicomputer.sms.data.model.PaymentStatus
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import com.saicomputer.sms.data.model.TERMINAL_STUDENT_STATUSES
import com.saicomputer.sms.feature.enrollments.displayCourseName
import kotlinx.coroutines.CoroutineScope
import com.saicomputer.sms.core.ui.theme.appDimens


private val HERO_STATUS_LABELS = mapOf(
    StudentStatus.New to "New",
    StudentStatus.Active to "Active",
    StudentStatus.PaymentPending to "Pmt Pending",
    StudentStatus.Completed to "Completed",
    StudentStatus.Dropout to "Dropout",
    StudentStatus.NotTakenAdmission to "Not Admitted"
)
private val GENDER_LABELS = mapOf(
    Gender.Male to "Male",
    Gender.Female to "Female",
    Gender.Other to "Other",
    Gender.PreferNotToSay to "Prefer not to say"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val coursesById by viewModel.coursesById.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(studentId) { viewModel.load(studentId) }
    androidx.compose.runtime.LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { snackbarController.show(scope, it) }
    }

    CrossfadeUiState(
        state = state,
        loading = {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                StudentDetailHeader(title = "Student", onBack = onBack, onEdit = null)
                LoadingSkeleton(modifier = Modifier.fillMaxSize())
            }
        },
        error = { message ->
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                StudentDetailHeader(title = "Student", onBack = onBack, onEdit = null)
                ErrorState(message = message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize())
            }
        },
        success = { data ->
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::manualRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                StudentDetailContent(
                    student = data.student,
                    enrollments = data.enrollments.orEmpty(),
                    payments = data.payments.orEmpty(),
                    coursesById = coursesById,
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
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudentDetailContent(
    student: Student,
    enrollments: List<Enrollment>,
    payments: List<Payment>,
    coursesById: Map<String, Course>,
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
    val showReactivate = canChangeStatus && student.status == StudentStatus.NotTakenAdmission
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
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
            val activePayments = payments.filter { it.status != PaymentStatus.Voided }
            StudentHeroCard(
                student = student,
                photoBase64 = photoState.file?.base64,
                photoLoading = photoState.loading,
                enrollmentCount = enrollments.size,
                paymentCount = activePayments.size,
                totalPaid = activePayments.sumOf { it.amount }
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = appDimens().spacingLg,
                divider = {},
                indicator = { positions ->
                    if (selectedTab < positions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                            height = appDimens().cornerRadiusProgress,
                            color = MaterialTheme.colorScheme.tertiary
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
                                color = if (selectedTab == index) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.padding(appDimens().spacingLg),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
            ) {
                when (selectedTab) {
                    0 -> ProfileTab(student)
                    1 -> EnrollmentsTab(enrollments, coursesById, onOpenEnrollment)
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
                Spacer(Modifier.height(appDimens().fabScrollClearance))
            }
            }
        }

        StudentFloatingActions(
            canEdit = canEdit,
            canChangeStatus = canChangeStatus,
            isTerminal = isTerminal,
            showReactivate = showReactivate,
            hasEnrollments = hasEnrollments,
            hasOngoingEnrollment = ongoingEnrollment != null,
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
            onReactivate = { statusDialog = StatusDialogMode.Reactivate },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = appDimens().spacingLg, bottom = appDimens().spacingLg)
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
                overflow = TextOverflow.Ellipsis,
                modifier = if (onEdit != null) Modifier.weight(1f) else Modifier
            )
            if (onEdit != null) {
                Box(
                    modifier = Modifier
                        .size(appDimens().iconSizeXxl)
                        .clip(CircleShape)
                        .clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit student",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentHeroCard(
    student: Student,
    photoBase64: String?,
    photoLoading: Boolean,
    enrollmentCount: Int,
    paymentCount: Int,
    totalPaid: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(appDimens().spacingLg),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacing14),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentPhotoAvatar(
                    name = student.fullName,
                    base64 = photoBase64,
                    loading = photoLoading,
                    size = 80
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
                ) {
                    Text(
                        student.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        student.studentId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
                ) {
                    Pill(
                        text = HERO_STATUS_LABELS[student.status] ?: student.status.name,
                        color = studentStatusColor(student.status),
                        style = MaterialTheme.typography.labelMedium
                    )
                    StudentCallButton(phoneNumber = student.phoneNumber)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = appDimens().spacing14),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroStatColumn(
                    value = enrollmentCount.toString(),
                    label = "Enrollments",
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    modifier = Modifier.height(appDimens().iconSizeXxl),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                HeroStatColumn(
                    value = paymentCount.toString(),
                    label = "Payments",
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(
                    modifier = Modifier.height(appDimens().iconSizeXxl),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                HeroStatColumn(
                    label = "Total paid",
                    modifier = Modifier.weight(1f),
                    valueContent = {
                        CurrencyText(
                            amount = totalPaid,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            bold = true
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentCallButton(phoneNumber: String) {
    val context = LocalContext.current
    val colors = appColors()

    IconButton(
        onClick = {
            context.startActivity(
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            )
        },
        modifier = Modifier
            .size(appDimens().callButtonSize)
            .clip(CircleShape)
            .background(colors.callActionContainer)
    ) {
        Icon(
            Icons.Outlined.Phone,
            contentDescription = "Call student",
            tint = colors.callAction,
            modifier = Modifier.size(appDimens().iconSizeMd)
        )
    }
}

@Composable
private fun HeroStatColumn(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    valueContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)
    ) {
        if (valueContent != null) {
            valueContent()
        } else {
            Text(
                text = value.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingXs),
            content = content
        )
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = appDimens().spacingMd)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing
        )
        Spacer(Modifier.height(appDimens().spacing10))
        Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingNone), content = content)
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = appDimens().spacing10)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(appDimens().spacingXs))
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(appDimens().spacing10))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun EnrollmentsTab(
    enrollments: List<Enrollment>,
    coursesById: Map<String, Course>,
    onOpen: (String) -> Unit
) {
    if (enrollments.isEmpty()) {
        EmptyTabMessage("No enrollments yet.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)) {
        enrollments.forEach { enrollment ->
            EnrollmentCard(
                enrollment = enrollment,
                courseDisplayName = enrollment.displayCourseName(coursesById),
                onClick = { onOpen(enrollment.enrollmentId) }
            )
        }
    }
}

@Composable
private fun EnrollmentCard(
    enrollment: Enrollment,
    courseDisplayName: String,
    onClick: () -> Unit
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
    val feePercent = if (enrollment.totalAmountDue > 0) {
        (enrollment.totalAmountPaid * 100 / enrollment.totalAmountDue).coerceIn(0, 100)
    } else 0
    val topicsPercent = enrollment.topicsSummary?.progressPercent ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    courseDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Pill(statusLabel, statusColor, style = MaterialTheme.typography.labelMedium)
            }
            Text(
                "${Formatters.formatDateIst(enrollment.startDate)} → ${Formatters.formatDateIst(enrollment.expectedEndDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            enrollment.billingType?.let { billing ->
                Pill(
                    text = when (billing) {
                        BillingType.Installment -> "Installment"
                        BillingType.Subscription -> "Subscription"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            FeeProgressBar(label = "Fee paid", percent = feePercent, color = MaterialTheme.colorScheme.primary)
            if (enrollment.topicsSummary?.hasTopics == true) {
                FeeProgressBar(label = "Topics", percent = topicsPercent, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun FeeProgressBar(label: String, percent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(appDimens().spacing6)
                .clip(appDimens().pillShape),
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
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
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
                onViewReceipt = onViewReceipt,
                showVoid = canVoid && !isVoided,
                onVoid = onVoid
            )
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
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = appDimens().cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
        ) {
            Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
                Text("Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(appDimens().chartHeightPie)
                        .dashedBorder(
                            MaterialTheme.colorScheme.outlineVariant,
                            appDimens().strokeDashed,
                            appDimens().spacingMd
                        )
                        .background(MaterialTheme.colorScheme.background, appDimens().fieldShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        photoLoading -> ThemedShimmerCircle(size = appDimens().avatarSizeHero)
                        photoBase64 != null -> {
                            val bitmap = rememberBase64ImageBitmap(photoBase64)
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap,
                                    contentDescription = "Student photo",
                                    modifier = Modifier
                                        .size(appDimens().avatarSizeHero)
                                        .clip(appDimens().pillShape)
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
                    shape = appDimens().fieldShape
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(appDimens().iconSizeSm))
                    Spacer(Modifier.size(appDimens().spacingSm))
                    Text(if (busy) "Uploading…" else "Replace Photo")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = appDimens().cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
        ) {
            Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
                Text("Aadhaar Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Masked: ${Formatters.maskAadhaar(student.aadhaarNumber).ifBlank { "Not set" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, appDimens().fieldShape)
                        .border(appDimens().strokeHairline, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), appDimens().fieldShape)
                        .padding(appDimens().spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeSm))
                    Text(
                        "Viewing this document will be logged for audit purposes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedButton(
                    onClick = onViewAadhaar,
                    modifier = Modifier.fillMaxWidth(),
                    shape = appDimens().fieldShape
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(appDimens().iconSizeSm))
                    Spacer(Modifier.size(appDimens().spacingSm))
                    Text("View Aadhaar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFloatingActions(
    canEdit: Boolean,
    canChangeStatus: Boolean,
    isTerminal: Boolean,
    showReactivate: Boolean,
    hasEnrollments: Boolean,
    hasOngoingEnrollment: Boolean,
    onNewEnrollment: () -> Unit,
    onRecordPayment: () -> Unit,
    onDropout: () -> Unit,
    onNotTakenAdmission: () -> Unit,
    onReactivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showFab = canEdit || (canChangeStatus && !isTerminal) || showReactivate
    if (!showFab) return

    var showActionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { showActionsSheet = true },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = appDimens().spacingXs)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Student actions")
        }
    }

    if (showActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            StudentActionsBottomSheet(
                canEdit = canEdit,
                canChangeStatus = canChangeStatus && !isTerminal,
                showReactivate = showReactivate,
                hasEnrollments = hasEnrollments,
                hasOngoingEnrollment = hasOngoingEnrollment,
                onNewEnrollment = {
                    showActionsSheet = false
                    onNewEnrollment()
                },
                onRecordPayment = {
                    showActionsSheet = false
                    onRecordPayment()
                },
                onDropout = {
                    showActionsSheet = false
                    onDropout()
                },
                onNotTakenAdmission = {
                    showActionsSheet = false
                    onNotTakenAdmission()
                },
                onReactivate = {
                    showActionsSheet = false
                    onReactivate()
                }
            )
        }
    }
}

@Composable
private fun StudentActionsBottomSheet(
    canEdit: Boolean,
    canChangeStatus: Boolean,
    showReactivate: Boolean,
    hasEnrollments: Boolean,
    hasOngoingEnrollment: Boolean,
    onNewEnrollment: () -> Unit,
    onRecordPayment: () -> Unit,
    onDropout: () -> Unit,
    onNotTakenAdmission: () -> Unit,
    onReactivate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = appDimens().spacingLg)
    ) {
        Text(
            text = "Student actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = appDimens().iconSizeLg, vertical = appDimens().spacingSm)
        )

        if (canEdit) {
            StudentActionSheetRow(
                icon = Icons.Outlined.Add,
                label = "New Enrollment",
                onClick = onNewEnrollment
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = appDimens().iconSizeLg)
            )
            StudentActionSheetRow(
                icon = Icons.Outlined.CreditCard,
                label = "Record a Payment",
                enabled = hasOngoingEnrollment,
                subtitle = if (!hasOngoingEnrollment) "Requires an ongoing enrollment" else null,
                onClick = onRecordPayment
            )
        }

        if (canChangeStatus) {
            if (canEdit) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = appDimens().iconSizeLg)
                )
            }
            StudentActionSheetRow(
                icon = Icons.Outlined.PersonOff,
                label = "Dropout",
                enabled = hasEnrollments,
                subtitle = if (!hasEnrollments) "Requires at least one enrollment" else null,
                onClick = onDropout
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = appDimens().iconSizeLg)
            )
            StudentActionSheetRow(
                icon = Icons.Outlined.EventBusy,
                label = "Not Admitted",
                enabled = !hasEnrollments,
                subtitle = if (hasEnrollments) "Only available before enrollment" else null,
                onClick = onNotTakenAdmission
            )
        }

        if (showReactivate) {
            if (canEdit || canChangeStatus) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = appDimens().iconSizeLg)
                )
            }
            StudentActionSheetRow(
                icon = Icons.Outlined.Replay,
                label = "Reactivate Student",
                onClick = onReactivate
            )
        }
    }
}

@Composable
private fun StudentActionSheetRow(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    subtitle: String? = null,
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
            tint = if (enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
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
private fun EmptyTabMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(appDimens().spacing28),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Modifier.dashedBorder(color: Color, strokeWidth: Dp, cornerRadius: Dp): Modifier = drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    val corner = cornerRadius.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidthPx, pathEffect = dash),
        cornerRadius = CornerRadius(corner, corner)
    )
}
