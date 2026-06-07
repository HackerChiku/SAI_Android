package com.saicomputer.sms.feature.enrollments

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.AmountField
import com.saicomputer.sms.core.ui.BackdateToggle
import com.saicomputer.sms.core.ui.DatePickerField
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.InstallmentType
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentWizardScreen(
    studentId: String?,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
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

    Scaffold(topBar = { SmsTopBar(title = "New Enrollment", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Step ${state.step + 1} of 3", style = MaterialTheme.typography.labelSmall)

            when (state.step) {
                0 -> {
                    Text("Select a course", style = MaterialTheme.typography.titleMedium)
                    if (state.coursesLoading) {
                        CircularProgressIndicator()
                    } else {
                        state.courses.forEach { c ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.selectCourse(c) }
                            ) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(c.courseName, fontWeight = FontWeight.SemiBold)
                                        Text(c.courseFullName, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (state.selectedCourse?.courseId == c.courseId) Text("✓")
                                }
                            }
                        }
                        Button(
                            onClick = { viewModel.goToStep(1) },
                            enabled = state.selectedCourse != null,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Next") }
                    }
                }

                1 -> {
                    Text("Dates & Fees", style = MaterialTheme.typography.titleMedium)
                    DatePickerField(state.startDate, { d -> viewModel.update { it.copy(startDate = d) } }, "Start date")
                    if (isSubscription) {
                        DatePickerField(state.subscriptionEndDate, { d -> viewModel.update { it.copy(subscriptionEndDate = d) } }, "Expected end date")
                        AmountField(state.effectiveFee, { v -> viewModel.update { it.copy(effectiveFee = v) } }, "Monthly fee", Modifier.fillMaxWidth())
                    } else {
                        Text("Installment type")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InstallmentType.entries.forEach { t ->
                                FilterChip(
                                    selected = state.installmentType == t,
                                    onClick = { viewModel.update { it.copy(installmentType = t) } },
                                    label = { Text(t.name) }
                                )
                            }
                        }
                        AmountField(state.effectiveFee, { v -> viewModel.update { it.copy(effectiveFee = v) } }, "Course fee", Modifier.fillMaxWidth())
                    }
                    AmountField(state.effectiveEnrollmentFee, { v -> viewModel.update { it.copy(effectiveEnrollmentFee = v) } }, "Enrollment fee", Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.goToStep(0) }, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = { viewModel.loadPreview(msg) },
                            enabled = !state.previewLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (state.previewLoading) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                            else Text(if (isSubscription) "Next" else "Preview")
                        }
                    }
                }

                2 -> {
                    Text("Review & Confirm", style = MaterialTheme.typography.titleMedium)
                    state.preview?.let { p ->
                        if (p.enrollmentFeeWaived && p.waivedFromCourseName != null) {
                            EnrollmentFeeWaiverBanner(p.waivedFromCourseName)
                        }
                    }
                    if (!isSubscription) {
                        InstallmentEditor(
                            rows = state.rows,
                            totalRequired = state.effectiveFee + state.effectiveEnrollmentFee,
                            ceiling = course?.maxInstallments ?: 12,
                            startDate = state.startDate,
                            editable = true,
                            onRowsChange = { rows -> viewModel.update { it.copy(rows = rows) } }
                        )
                    } else {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Monthly fee ${Formatters.formatInr(state.effectiveFee)}")
                                Text("Enrollment fee ${Formatters.formatInr(state.effectiveEnrollmentFee)}")
                            }
                        }
                    }

                    if (can(user, "system.backdate")) {
                        BackdateToggle(
                            enabled = state.backdateEnabled,
                            onEnabledChange = { v -> viewModel.update { it.copy(backdateEnabled = v) } },
                            date = state.effectiveCreatedAt,
                            onDateChange = { d -> viewModel.update { it.copy(effectiveCreatedAt = d) } },
                            dateLabel = "Effective created date"
                        )
                    }

                    val canSubmit = if (isSubscription) state.effectiveFee > 0 else
                        installmentsValid(state.rows, state.effectiveFee + state.effectiveEnrollmentFee, course?.maxInstallments ?: 12, state.startDate)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.goToStep(1) }, modifier = Modifier.weight(1f)) { Text("Back") }
                        Button(
                            onClick = {
                                viewModel.submit(
                                    onSuccess = { id -> msg("Enrollment created"); onCreated(id) },
                                    onError = msg
                                )
                            },
                            enabled = canSubmit && !state.submitting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (state.submitting) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
                            else Text("Create")
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
