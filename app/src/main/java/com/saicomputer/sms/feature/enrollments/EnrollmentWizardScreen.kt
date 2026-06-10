package com.saicomputer.sms.feature.enrollments

import com.saicomputer.sms.core.ui.studentStatusColor
import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.AmountField
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.BackdateEntryCard
import com.saicomputer.sms.core.ui.TitleBarBackButton
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.DatePickerField
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.InstallmentType
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.saicomputer.sms.core.ui.theme.appDimens


private val STUDENT_STATUS_LABELS = mapOf(
    StudentStatus.New to "New",
    StudentStatus.Active to "Active",
    StudentStatus.PaymentPending to "Pmt Pending",
    StudentStatus.Completed to "Completed",
    StudentStatus.Dropout to "Dropout",
    StudentStatus.NotTakenAdmission to "Not Admitted"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentWizardScreen(
    studentId: String?,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    onCreateStudent: () -> Unit = {},
    snackbarController: SnackbarController,
    viewModel: EnrollmentWizardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(studentId) { viewModel.initialize(studentId) }

    val msg: (String) -> Unit = { snackbarController.show(scope, it) }
    val course = state.selectedCourse
    val isSubscription = course?.billingType == BillingType.Subscription

    Column(modifier = Modifier.fillMaxSize()) {
        WizardHeader(onBack = onBack)
        WizardStepper(
            currentStep = state.displayStep,
            skipStudentStep = state.skipStudentStep
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
            ) {
                when (state.step) {
                    0 -> StudentStep(
                        state = state,
                        onSearchChange = viewModel::onStudentSearchChange,
                        onSelectStudent = viewModel::selectStudent,
                        onCreateStudent = onCreateStudent
                    )
                    1 -> CourseStep(
                        state = state,
                        onSelectCourse = viewModel::selectCourse
                    )
                    2 -> ScheduleStep(
                        state = state,
                        user = user,
                        isSubscription = isSubscription,
                        course = course,
                        onUpdate = viewModel::update,
                        onScheduleChanged = { viewModel.refreshPreview(msg) }
                    )
                }
            }

            WizardFooter(
                state = state,
                isSubscription = isSubscription,
                course = course,
                onBack = {
                    when (state.step) {
                        0 -> onBack()
                        else -> viewModel.goToStep(state.step - 1)
                    }
                },
                onNext = {
                    when (state.step) {
                        0 -> viewModel.nextFromStudent()
                        1 -> viewModel.nextFromCourse(msg)
                        else -> Unit
                    }
                },
                onSubmit = {
                    viewModel.submit(
                        onSuccess = { id -> msg("Enrollment created"); onCreated(id) },
                        onError = msg
                    )
                }
            )
        }
    }
}

@Composable
private fun WizardHeader(onBack: () -> Unit) {
    AppTopBarBox {
        AppTitleBarRow(
            leading = {
                TitleBarBackButton(onBack = onBack)
                Text(
                    "New Enrollment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}

@Composable
private fun WizardStepper(currentStep: Int, skipStudentStep: Boolean) {
    val steps = if (skipStudentStep) listOf("Course", "Schedule") else listOf("Student", "Course", "Schedule")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().spacingLg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = if (skipStudentStep) index + 2 else index + 1
            val isCompleted = stepNumber < currentStep
            val isActive = stepNumber == currentStep

            if (index > 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(appDimens().spacingXxs)
                        .padding(horizontal = appDimens().spacing6)
                        .background(if (stepNumber <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(appDimens().spacing32)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                isActive -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(appDimens().iconSizeSm))
                    } else {
                        Text(
                            stepNumber.toString(),
                            color = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
                Spacer(Modifier.height(appDimens().spacingXs))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.tertiary
                        isCompleted -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun StudentStep(
    state: WizardState,
    onSearchChange: (String) -> Unit,
    onSelectStudent: (Student) -> Unit,
    onCreateStudent: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
        Text("Search student", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = state.studentSearch,
            onValueChange = onSearchChange,
            placeholder = { Text("Name, phone, or ID") },
            shape = appDimens().fieldShape,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        if (state.studentsLoading) {
            LoadingSkeleton(modifier = Modifier.fillMaxWidth(), rows = 3)
        } else {
            state.students.forEach { student ->
                val selected = state.studentId == student.studentId
                StudentSelectCard(
                    student = student,
                    selected = selected,
                    onClick = { onSelectStudent(student) }
                )
            }
        }

        OutlinedButton(
            onClick = onCreateStudent,
            modifier = Modifier.fillMaxWidth(),
            shape = appDimens().fieldShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
        ) {
            Text("+ Create New Student", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StudentSelectCard(student: Student, selected: Boolean, onClick: () -> Unit) {
    val statusColor = studentStatusColor(student.status)
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(appDimens().strokeHairline, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(appDimens().spacing14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
        ) {
            ColoredPhotoAvatar(name = student.fullName, size = 44)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
                Text(student.fullName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${student.studentId} · ${student.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeListInner))
            }
            Pill(
                text = STUDENT_STATUS_LABELS[student.status] ?: student.status.name,
                color = statusColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CourseStep(state: WizardState, onSelectCourse: (Course) -> Unit) {
    state.selectedStudent?.let { student ->
        StudentSummaryCard(student)
    }

    if (state.coursesLoading) {
        LoadingSkeleton(modifier = Modifier.fillMaxWidth(), rows = 3)
    } else {
        state.courses.forEach { course ->
            CourseSelectCard(
                course = course,
                selected = state.selectedCourse?.courseId == course.courseId,
                onClick = { onSelectCourse(course) }
            )
        }
    }
}

@Composable
private fun StudentSummaryCard(student: Student) {
    val statusColor = studentStatusColor(student.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(appDimens().spacing14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
        ) {
            ColoredPhotoAvatar(name = student.fullName, size = 44)
            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, fontWeight = FontWeight.Bold)
                Text(student.studentId, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Pill(
                text = STUDENT_STATUS_LABELS[student.status] ?: student.status.name,
                color = statusColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun CourseSelectCard(course: Course, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val feeLabel = if (course.billingType == BillingType.Subscription) {
        Formatters.formatInr(course.monthlyFee) + "/mo"
    } else {
        Formatters.formatInr(course.fee)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(appDimens().strokeHairline, borderColor)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (selected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeListInner))
                }
            }
            Text(
                "${course.courseFullName} · ${course.durationMonths} months",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
                Pill(
                    text = if (course.billingType == BillingType.Subscription) "Subscription" else "Installment",
                    color = appColors().info,
                    style = MaterialTheme.typography.labelMedium
                )
                Pill(text = "Fee: $feeLabel", color = appColors().neutral, style = MaterialTheme.typography.labelMedium)
                if (course.generateCertificate) {
                    Pill(text = "Certificate ✓", color = appColors().success, style = MaterialTheme.typography.labelMedium)
                }
            }
            if (course.billingType == BillingType.Installment) {
                Text(
                    "Up to ${course.maxInstallments} installments",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (course.hasTopics) {
                val count = course.topicsCount ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(appDimens().fieldShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(appDimens().spacing10),
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeSm))
                    Text(
                        "This course has $count topic${if (count == 1) "" else "s"} that will be copied as a checklist to the enrollment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleStep(
    state: WizardState,
    user: com.saicomputer.sms.data.model.User?,
    isSubscription: Boolean,
    course: Course?,
    onUpdate: ((WizardState) -> WizardState) -> Unit,
    onScheduleChanged: () -> Unit
) {
    if (can(user, "system.backdate")) {
        BackdateEntryCard(
            enabled = state.backdateEnabled,
            onEnabledChange = { v -> onUpdate { it.copy(backdateEnabled = v) } },
            date = state.effectiveCreatedAt,
            onDateChange = { d -> onUpdate { it.copy(effectiveCreatedAt = d) } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
            Text(
                if (isSubscription) "Schedule — Subscription" else "Schedule — Installment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DatePickerField(
                value = state.startDate,
                onValueChange = { d ->
                    onUpdate { it.copy(startDate = d) }
                    if (!isSubscription) onScheduleChanged()
                },
                label = "Start date"
            )
            if (isSubscription) {
                DatePickerField(
                    value = state.subscriptionEndDate.ifBlank { state.startDate },
                    onValueChange = { d -> onUpdate { it.copy(subscriptionEndDate = d) } },
                    label = "Expected end date"
                )
            } else {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = state.installmentType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Installment type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = appDimens().fieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        InstallmentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    onUpdate { it.copy(installmentType = type) }
                                    expanded = false
                                    onScheduleChanged()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (!isSubscription) {
        if (state.previewLoading) {
            LoadingSkeleton(modifier = Modifier.fillMaxWidth(), rows = 3)
        } else {
            state.preview?.let { preview ->
                if (preview.enrollmentFeeWaived && preview.waivedFromCourseName != null) {
                    EnrollmentFeeWaiverBanner(preview.waivedFromCourseName)
                }
                InstallmentSchedulePreview(
                    rows = state.rows,
                    startDate = state.startDate,
                    total = state.effectiveFee + state.effectiveEnrollmentFee
                )
            }
        }
    }

    CustomizeFeesSection(
        state = state,
        course = course,
        isSubscription = isSubscription,
        onUpdate = onUpdate,
        onScheduleChanged = onScheduleChanged
    )
}

@Composable
private fun InstallmentSchedulePreview(
    rows: List<EditableInstallment>,
    startDate: String,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
            Text("Installment Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(appDimens().fieldShape)
                        .background(bg)
                        .padding(horizontal = appDimens().spacingMd, vertical = appDimens().spacing10),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = appDimens().spacingSm))
                    Text(Formatters.formatInr(row.amountDue), fontWeight = FontWeight.Medium)
                    Text(
                        relativeDueLabel(startDate, row.dueDate, index + 1),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.SemiBold)
                Text(Formatters.formatInr(total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CustomizeFeesSection(
    state: WizardState,
    course: Course?,
    isSubscription: Boolean,
    onUpdate: ((WizardState) -> WizardState) -> Unit,
    onScheduleChanged: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdate { it.copy(customizeFeesExpanded = !it.customizeFeesExpanded) } }
                .padding(vertical = appDimens().spacingXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacing6)
        ) {
            Icon(
                if (state.customizeFeesExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Customize Fees", fontWeight = FontWeight.SemiBold)
        }
        if (state.customizeFeesExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = appDimens().cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(appDimens().spacingLg), verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
                    AmountField(
                        value = state.effectiveFee,
                        onValueChange = { v ->
                            onUpdate { it.copy(effectiveFee = v) }
                            if (!isSubscription) onScheduleChanged()
                        },
                        label = if (isSubscription) "Monthly Fee (₹)" else "Course Fee (₹)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Course default: ${Formatters.formatInr(if (isSubscription) course?.monthlyFee ?: 0 else course?.fee ?: 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AmountField(
                        value = state.effectiveEnrollmentFee,
                        onValueChange = { v ->
                            onUpdate { it.copy(effectiveEnrollmentFee = v) }
                            if (!isSubscription) onScheduleChanged()
                        },
                        label = "Enrollment Fee (₹)",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Course default: ${Formatters.formatInr(course?.enrollmentFee ?: 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardFooter(
    state: WizardState,
    isSubscription: Boolean,
    course: Course?,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    val onSchedule = state.step == 2
    val canNext = when (state.step) {
        0 -> state.canProceedFromStudent
        1 -> state.canProceedFromCourse && !state.previewLoading
        else -> false
    }
    val canSubmit = if (onSchedule) {
        if (isSubscription) {
            state.effectiveFee > 0
        } else {
            installmentsValid(
                state.rows,
                state.effectiveFee + state.effectiveEnrollmentFee,
                course?.maxInstallments ?: 12,
                state.startDate
            )
        }
    } else false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
    ) {
        if (onSchedule || state.step > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = appDimens().fieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Back", fontWeight = FontWeight.SemiBold)
            }
        }
        if (onSchedule) {
            Button(
                onClick = onSubmit,
                enabled = canSubmit && !state.submitting,
                modifier = Modifier.weight(2f),
                shape = appDimens().fieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.surface)
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(appDimens().iconSizeMd), color = MaterialTheme.colorScheme.surface, strokeWidth = appDimens().spacingXxs)
                } else {
                    Text("Create Enrollment", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Button(
                onClick = onNext,
                enabled = canNext,
                modifier = Modifier.weight(if (state.step > 0) 2f else 1f),
                shape = appDimens().fieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canNext) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant,
                    contentColor = if (canNext) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Next", fontWeight = FontWeight.SemiBold)
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

private fun relativeDueLabel(startDate: String, dueDate: String, fallbackIndex: Int): String {
    val months = runCatching {
        val start = LocalDate.parse(startDate)
        val due = LocalDate.parse(dueDate)
        ChronoUnit.MONTHS.between(start, due).coerceAtLeast(1)
    }.getOrDefault(fallbackIndex.toLong())
    return "Due: $months month(s)"
}
