package com.saicomputer.sms.feature.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.AmountField
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.TopicDurationUnit
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun CourseFormScreen(
    courseId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: CourseFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(courseId) { viewModel.initialize(courseId) }

    Scaffold(
        topBar = { SmsTopBar(title = if (state.isEdit) "Edit Course" else "New Course", onBack = onBack) }
    ) { padding ->
        if (state.loading) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.padding(16.dp))
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
            Field("Course name *", state.courseName) { v -> viewModel.update { it.copy(courseName = v) } }
            Field("Full name *", state.courseFullName) { v -> viewModel.update { it.copy(courseFullName = v) } }
            Field("Description", state.description) { v -> viewModel.update { it.copy(description = v) } }
            Field("Course link", state.courseLink) { v -> viewModel.update { it.copy(courseLink = v) } }
            NumberField("Duration (months)", state.durationMonths) { v -> viewModel.update { it.copy(durationMonths = v) } }
            AmountField(state.fee, { v -> viewModel.update { it.copy(fee = v) } }, "Total fee", Modifier.fillMaxWidth())
            AmountField(state.enrollmentFee, { v -> viewModel.update { it.copy(enrollmentFee = v) } }, "Enrollment fee", Modifier.fillMaxWidth())
            NumberField(
                "Max installments allowed",
                state.maxInstallments,
                supporting = "Max installments any enrollment of this course can have. Default 12."
            ) { v -> viewModel.update { it.copy(maxInstallments = v) } }

            Text("Billing Type", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillingType.entries.forEach { bt ->
                    FilterChip(
                        selected = state.billingType == bt,
                        onClick = { viewModel.update { it.copy(billingType = bt) } },
                        label = { Text(bt.name) }
                    )
                }
            }

            if (state.billingType == BillingType.Subscription) {
                AmountField(state.monthlyFee, { v -> viewModel.update { it.copy(monthlyFee = v) } }, "Monthly fee", Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("3+1 Package")
                    Switch(
                        checked = state.packageType == PackageType.PACKAGE_3_1,
                        onCheckedChange = { on ->
                            viewModel.update { it.copy(packageType = if (on) PackageType.PACKAGE_3_1 else PackageType.NONE) }
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Generate certificate")
                Switch(checked = state.generateCertificate, onCheckedChange = { on -> viewModel.update { it.copy(generateCertificate = on) } })
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Has topics")
                Switch(checked = state.hasTopics, onCheckedChange = { on -> viewModel.update { it.copy(hasTopics = on) } })
            }

            if (state.hasTopics) {
                CourseTopicsEditor(viewModel)
            }

            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)

            Button(
                onClick = {
                    viewModel.submit(
                        onSaved = { onSaved() },
                        onMessage = { msg -> snackbarController.show(scope, msg) }
                    )
                },
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (state.isEdit) "Save Course" else "Create Course")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CourseTopicsEditor(viewModel: CourseFormViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Topics", style = MaterialTheme.typography.titleMedium)
        Text(
            "Each topic is copied to new enrollments as an immutable checklist.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.topics.forEach { topic ->
            val err = state.topicErrors[topic.localId]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = topic.topicName,
                            onValueChange = { v -> viewModel.updateTopic(topic.localId) { it.copy(topicName = v) } },
                            label = { Text("Topic name") },
                            isError = err != null,
                            supportingText = err?.let { { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeTopic(topic.localId) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove")
                        }
                    }
                    OutlinedTextField(
                        value = topic.description,
                        onValueChange = { v -> viewModel.updateTopic(topic.localId) { it.copy(description = v) } },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = if (topic.durationValue == 0) "" else topic.durationValue.toString(),
                            onValueChange = { v -> viewModel.updateTopic(topic.localId) { it.copy(durationValue = v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0) } },
                            label = { Text("Duration") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(120.dp)
                        )
                        TopicDurationUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = topic.durationUnit == unit,
                                onClick = { viewModel.updateTopic(topic.localId) { it.copy(durationUnit = unit) } },
                                label = { Text(unit.name) }
                            )
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = { viewModel.addTopic() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text("Add topic")
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberField(label: String, value: Int, supporting: String? = null, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { v -> onChange(v.filter { it.isDigit() }.toIntOrNull() ?: 0) },
        label = { Text(label) },
        supportingText = supporting?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
