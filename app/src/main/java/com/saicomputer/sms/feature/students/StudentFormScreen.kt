package com.saicomputer.sms.feature.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.REGISTRATION_SESSION_LABELS
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.BackdateToggle
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.RegistrationSession

@Composable
fun StudentFormScreen(
    studentId: String?,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    snackbarController: SnackbarController,
    viewModel: StudentFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(studentId) { viewModel.initialize(studentId) }

    Scaffold(
        topBar = {
            SmsTopBar(title = if (state.isEdit) "Edit Student" else "New Student", onBack = onBack)
        }
    ) { padding ->
        if (state.loading) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Backdate (Owner only, create mode only)
            if (!state.isEdit && can(user, "system.backdate")) {
                BackdateToggle(
                    enabled = state.backdateEnabled,
                    onEnabledChange = { v -> viewModel.update { it.copy(backdateEnabled = v) } },
                    date = state.effectiveCreatedAt,
                    onDateChange = { d -> viewModel.update { it.copy(effectiveCreatedAt = d) } },
                    dateLabel = "Effective created date"
                )
            }

            Text("Registration Session", style = MaterialTheme.typography.titleMedium)
            RegistrationSession.entries.forEach { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.registrationSession == session,
                            onClick = { viewModel.onRegistrationSessionChange(session) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.registrationSession == session,
                        onClick = { viewModel.onRegistrationSessionChange(session) }
                    )
                    Text(REGISTRATION_SESSION_LABELS[session] ?: session.name)
                }
            }
            if (state.registrationSession != RegistrationSession.NewRecord) {
                OutlinedTextField(
                    value = state.oldRegistrationNumber,
                    onValueChange = { v -> viewModel.update { it.copy(oldRegistrationNumber = v) } },
                    label = { Text("Old Registration Number *") },
                    isError = state.oldRegError != null,
                    supportingText = state.oldRegError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Basic Information", style = MaterialTheme.typography.titleMedium)
            FormField("Full name *", state.fullName) { v -> viewModel.update { it.copy(fullName = v) } }
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { v -> viewModel.update { it.copy(phoneNumber = v.filter { c -> c.isDigit() }.take(10)) } },
                label = { Text("Phone (10 digits) *") },
                isError = state.phoneError != null,
                supportingText = state.phoneError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = { v -> viewModel.update { it.copy(email = v) } },
                label = { Text("Email") },
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FormField("Parent name", state.parentName) { v -> viewModel.update { it.copy(parentName = v) } }
            OutlinedTextField(
                value = state.parentPhone,
                onValueChange = { v -> viewModel.update { it.copy(parentPhone = v.filter { c -> c.isDigit() }.take(10)) } },
                label = { Text("Parent phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            FormField("Address", state.address) { v -> viewModel.update { it.copy(address = v) } }
            FormField("Academic qualification", state.academicQualification) { v -> viewModel.update { it.copy(academicQualification = v) } }
            FormField("Last institution", state.lastInstitution) { v -> viewModel.update { it.copy(lastInstitution = v) } }
            FormField("Additional notes", state.additionalNotes) { v -> viewModel.update { it.copy(additionalNotes = v) } }

            Text("Aadhaar", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = if (state.aadhaarLoading) "" else state.aadhaarNumber,
                onValueChange = viewModel::onAadhaarChange,
                label = { Text(if (state.aadhaarLoading) "Loading…" else "Aadhaar number (12 digits)") },
                enabled = !state.aadhaarLoading,
                isError = state.aadhaarError != null,
                supportingText = state.aadhaarError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    viewModel.submit { id ->
                        snackbarController.show(scope, if (state.isEdit) "Student updated" else "Student created")
                        onSaved(id)
                    }
                },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (state.isEdit) "Save Changes" else "Create Student")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
