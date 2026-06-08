package com.saicomputer.sms.feature.enrollments

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.AmountField
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.DatePickerField
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.core.ui.theme.StatusZinc
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.InstallmentType
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private val BackdateOrange = Color(0xFFEA580C)
private val BackdateOrangeTint = Color(0xFFFFF7ED)

private val STUDENT_STATUS_LABELS = mapOf(
    StudentStatus.New to "New",
    StudentStatus.Active to "Active",
    StudentStatus.PaymentPending to "Pmt Pending",
    StudentStatus.Completed to "Completed",
    StudentStatus.Dropout to "Dropout",
    StudentStatus.NotTakenAdmission to "Not Admitted"
)

private val STUDENT_STATUS_COLORS = mapOf(
    StudentStatus.New to StatusBlue,
    StudentStatus.Active to StatusEmerald,
    StudentStatus.PaymentPending to StatusAmber,
    StudentStatus.Completed to StatusGray,
    StudentStatus.Dropout to StatusRed,
    StudentStatus.NotTakenAdmission to StatusZinc
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
                .background(OffWhite)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BaseWhite)
        }
        Text(
            "New Enrollment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite
        )
    }
}

@Composable
private fun WizardStepper(currentStep: Int, skipStudentStep: Boolean) {
    val steps = if (skipStudentStep) listOf("Course", "Schedule") else listOf("Student", "Course", "Schedule")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BaseWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        .height(2.dp)
                        .padding(horizontal = 6.dp)
                        .background(if (stepNumber <= currentStep) BrandBlue else OutlineVariantLight)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> BrandBlue
                                isActive -> BrandRed
                                else -> OutlineVariantLight
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = BaseWhite, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            stepNumber.toString(),
                            color = if (isActive) BaseWhite else OnSurfaceVariantLightColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isActive -> BrandRed
                        isCompleted -> BrandBlue
                        else -> OnSurfaceVariantLightColor
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Search student", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = state.studentSearch,
            onValueChange = onSearchChange,
            placeholder = { Text("Name, phone, or ID") },
            shape = FieldShape,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = OutlineVariantLight,
                focusedContainerColor = BaseWhite,
                unfocusedContainerColor = BaseWhite
            ),
            singleLine = true
        )

        if (state.studentsLoading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
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
            shape = FieldShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BrandBlue))
        ) {
            Text("+ Create New Student", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StudentSelectCard(student: Student, selected: Boolean, onClick: () -> Unit) {
    val statusColor = STUDENT_STATUS_COLORS[student.status] ?: StatusGray
    val bgColor = if (selected) BrandBlueTint else BaseWhite
    val borderColor = if (selected) BrandBlue else OutlineVariantLight

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColoredPhotoAvatar(name = student.fullName, size = 44)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(student.fullName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${student.studentId} · ${student.phoneNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLightColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(22.dp))
            }
            Pill(
                text = STUDENT_STATUS_LABELS[student.status] ?: student.status.name,
                color = statusColor,
                fontSize = 10.sp
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
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlue)
        }
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
    val statusColor = STUDENT_STATUS_COLORS[student.status] ?: StatusGray
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColoredPhotoAvatar(name = student.fullName, size = 44)
            Column(modifier = Modifier.weight(1f)) {
                Text(student.fullName, fontWeight = FontWeight.Bold)
                Text(student.studentId, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
            }
            Pill(
                text = STUDENT_STATUS_LABELS[student.status] ?: student.status.name,
                color = statusColor,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CourseSelectCard(course: Course, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) BrandBlueTint else BaseWhite
    val borderColor = if (selected) BrandBlue else OutlineVariantLight
    val feeLabel = if (course.billingType == BillingType.Subscription) {
        Formatters.formatInr(course.monthlyFee) + "/mo"
    } else {
        Formatters.formatInr(course.fee)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (selected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                "${course.courseFullName} · ${course.durationMonths} months",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantLightColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(
                    text = if (course.billingType == BillingType.Subscription) "Subscription" else "Installment",
                    color = StatusBlue,
                    fontSize = 10.sp
                )
                Pill(text = "Fee: $feeLabel", color = StatusGray, fontSize = 10.sp)
                if (course.generateCertificate) {
                    Pill(text = "Certificate ✓", color = StatusEmerald, fontSize = 10.sp)
                }
            }
            if (course.billingType == BillingType.Installment) {
                Text(
                    "Up to ${course.maxInstallments} installments",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariantLightColor
                )
            }
            if (course.hasTopics) {
                val count = course.topicsCount ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FieldShape)
                        .background(BrandBlueTint)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                    Text(
                        "This course has $count topic${if (count == 1) "" else "s"} that will be copied as a checklist to the enrollment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandBlue
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        shape = FieldShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = OutlineVariantLight
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
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Installment Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) OffWhite else BaseWhite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FieldShape)
                        .background(bg)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 8.dp))
                    Text(Formatters.formatInr(row.amountDue), fontWeight = FontWeight.Medium)
                    Text(
                        relativeDueLabel(startDate, row.dueDate, index + 1),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariantLightColor
                    )
                }
            }
            HorizontalDivider(color = OutlineVariantLight)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.SemiBold)
                Text(Formatters.formatInr(total), fontWeight = FontWeight.Bold, color = BrandBlue)
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUpdate { it.copy(customizeFeesExpanded = !it.customizeFeesExpanded) } }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (state.customizeFeesExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = OnSurfaceVariantLightColor
            )
            Text("Customize Fees", fontWeight = FontWeight.SemiBold)
        }
        if (state.customizeFeesExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = BaseWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        color = OnSurfaceVariantLightColor
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
                        color = OnSurfaceVariantLightColor
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
            .background(BaseWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onSchedule || state.step > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = FieldShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
            ) {
                Text("Back", fontWeight = FontWeight.SemiBold)
            }
        }
        if (onSchedule) {
            Button(
                onClick = onSubmit,
                enabled = canSubmit && !state.submitting,
                modifier = Modifier.weight(2f),
                shape = FieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed, contentColor = BaseWhite)
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BaseWhite, strokeWidth = 2.dp)
                } else {
                    Text("Create Enrollment", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Button(
                onClick = onNext,
                enabled = canNext,
                modifier = Modifier.weight(if (state.step > 0) 2f else 1f),
                shape = FieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canNext) BrandRed else OutlineVariantLight,
                    contentColor = if (canNext) BaseWhite else OnSurfaceVariantLightColor,
                    disabledContainerColor = OutlineVariantLight,
                    disabledContentColor = OnSurfaceVariantLightColor
                )
            ) {
                Text("Next", fontWeight = FontWeight.SemiBold)
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun BackdateEntryCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    date: String?,
    onDateChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(BackdateOrange.copy(alpha = 0.65f))
            .background(BackdateOrangeTint, FieldShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = BackdateOrange, modifier = Modifier.size(26.dp))
            Text(
                "Record as backdated entry",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = BackdateOrange,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BaseWhite,
                    checkedTrackColor = BackdateOrange,
                    uncheckedThumbColor = BaseWhite,
                    uncheckedTrackColor = OutlineVariantLight
                )
            )
        }
        if (enabled) {
            DatePickerField(
                value = date ?: LocalDate.now().toString(),
                onValueChange = onDateChange,
                label = "Entry date"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = BackdateOrange, modifier = Modifier.size(18.dp))
                Text(
                    "No confirmation email will be sent automatically. Receipt PDF will still be generated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BackdateOrange
                )
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

private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
    drawRoundRect(color = color, cornerRadius = CornerRadius(12.dp.toPx()), style = stroke)
}
