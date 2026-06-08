package com.saicomputer.sms.feature.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineLight
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.TopicDurationUnit

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)

private val PACKAGE_LABELS = mapOf(
    PackageType.NONE to "None",
    PackageType.PACKAGE_3_1 to "3+1"
)

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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OutlineLight,
        unfocusedBorderColor = OutlineVariantLight,
        focusedContainerColor = BaseWhite,
        unfocusedContainerColor = BaseWhite,
        disabledContainerColor = BaseWhite,
        focusedPlaceholderColor = OnSurfaceVariantLightColor,
        unfocusedPlaceholderColor = OnSurfaceVariantLightColor
    )

    Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {
        CourseFormHeader(
            title = if (state.isEdit) "Edit Course" else "New Course",
            onBack = onBack
        )

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormSectionCard(title = "Course Info") {
                FormTextField(
                    label = "Course Code / Short Name",
                    required = true,
                    value = state.courseName,
                    onValueChange = { v -> viewModel.update { it.copy(courseName = v) } },
                    placeholder = "e.g. DCA",
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Full Name",
                    required = true,
                    value = state.courseFullName,
                    onValueChange = { v -> viewModel.update { it.copy(courseFullName = v) } },
                    placeholder = "Diploma in Computer Applications",
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Description",
                    value = state.description,
                    onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                    singleLine = false,
                    minLines = 3,
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Course Link / URL",
                    value = state.courseLink,
                    onValueChange = { v -> viewModel.update { it.copy(courseLink = v) } },
                    placeholder = "https://...",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    fieldColors = fieldColors
                )
                FormNumberField(
                    label = "Duration (months)",
                    value = state.durationMonths,
                    onValueChange = { v -> viewModel.update { it.copy(durationMonths = v) } },
                    fieldColors = fieldColors
                )
            }

            FormSectionCard(title = "Billing") {
                BillingTypeSelector(
                    selected = state.billingType,
                    onSelected = { bt -> viewModel.update { it.copy(billingType = bt) } }
                )

                if (state.billingType == BillingType.Installment) {
                    FormAmountField(
                        label = "Course Fee (₹)",
                        value = state.fee,
                        onValueChange = { v -> viewModel.update { it.copy(fee = v) } },
                        fieldColors = fieldColors
                    )
                    FormAmountField(
                        label = "Enrollment Fee (₹)",
                        value = state.enrollmentFee,
                        onValueChange = { v -> viewModel.update { it.copy(enrollmentFee = v) } },
                        fieldColors = fieldColors
                    )
                    FormNumberField(
                        label = "Max Installments Allowed",
                        value = state.maxInstallments,
                        onValueChange = { v -> viewModel.update { it.copy(maxInstallments = v) } },
                        helperText = "Default is 12. Students cannot split payment into more than this many parts.",
                        fieldColors = fieldColors
                    )
                } else {
                    FormAmountField(
                        label = "Monthly Fee (₹)",
                        value = state.monthlyFee,
                        onValueChange = { v -> viewModel.update { it.copy(monthlyFee = v) } },
                        fieldColors = fieldColors
                    )
                    FormAmountField(
                        label = "Enrollment Fee (₹)",
                        value = state.enrollmentFee,
                        onValueChange = { v -> viewModel.update { it.copy(enrollmentFee = v) } },
                        fieldColors = fieldColors
                    )
                    PackageDropdown(
                        selected = state.packageType,
                        onSelected = { pkg -> viewModel.update { it.copy(packageType = pkg) } },
                        fieldColors = fieldColors
                    )
                }
            }

            FormSectionCard(title = "Options") {
                OptionToggleRow(
                    title = "Generate Certificate",
                    subtitle = "Issue a PDF certificate on completion",
                    checked = state.generateCertificate,
                    onCheckedChange = { on -> viewModel.update { it.copy(generateCertificate = on) } }
                )
                OptionToggleRow(
                    title = "Has Topics",
                    subtitle = "Add a syllabus checklist to enrollments",
                    checked = state.hasTopics,
                    onCheckedChange = viewModel::setHasTopics
                )
            }

            if (state.hasTopics) {
                FormSectionCard(title = "Course Topics") {
                    TopicsInfoBox()
                    state.topics.forEachIndexed { index, topic ->
                        val err = state.topicErrors[topic.localId]
                        TopicEditorCard(
                            index = index,
                            topic = topic,
                            error = err,
                            onNameChange = { v -> viewModel.updateTopic(topic.localId) { it.copy(topicName = v) } },
                            onDescriptionChange = { v ->
                                viewModel.updateTopic(topic.localId) { it.copy(description = v) }
                            },
                            onDurationChange = { v ->
                                viewModel.updateTopic(topic.localId) {
                                    it.copy(durationValue = v.filter { c -> c.isDigit() }.toIntOrNull() ?: 0)
                                }
                            },
                            onUnitChange = { unit ->
                                viewModel.updateTopic(topic.localId) { it.copy(durationUnit = unit) }
                            },
                            onRemove = { viewModel.removeTopic(topic.localId) },
                            fieldColors = fieldColors
                        )
                    }
                    AddTopicButton(onClick = viewModel::addTopic)
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.submit(
                    onSaved = { onSaved() },
                    onMessage = { msg -> snackbarController.show(scope, msg) }
                )
            },
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = FieldShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandRed,
                contentColor = BaseWhite,
                disabledContainerColor = BrandRed.copy(alpha = 0.4f),
                disabledContentColor = BaseWhite.copy(alpha = 0.7f)
            )
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = BaseWhite
                )
            } else {
                Text(
                    if (state.isEdit) "Save Course" else "Create Course",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CourseFormHeader(title: String, onBack: () -> Unit) {
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
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun FormSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun FormLabel(label: String, required: Boolean = false) {
    Text(
        buildAnnotatedString {
            append(label)
            if (required) {
                withStyle(SpanStyle(color = BrandRed)) { append(" *") }
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorText: String? = null,
    helperText: String? = null,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel(label, required)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            isError = isError,
            shape = FieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        when {
            isError && errorText != null ->
                Text(errorText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            helperText != null ->
                Text(helperText, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
        }
    }
}

@Composable
private fun FormNumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    helperText: String? = null,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel(label, required)
        OutlinedTextField(
            value = if (value == 0) "" else value.toString(),
            onValueChange = { v -> onValueChange(v.filter { it.isDigit() }.toIntOrNull() ?: 0) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = FieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        helperText?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
        }
    }
}

@Composable
private fun FormAmountField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel(label)
        OutlinedTextField(
            value = if (value == 0) "" else value.toString(),
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(9)
                onValueChange(digits.toIntOrNull() ?: 0)
            },
            prefix = { Text("₹") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = FieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BillingTypeSelector(
    selected: BillingType,
    onSelected: (BillingType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .border(1.dp, OutlineVariantLight, FieldShape)
    ) {
        BillingType.entries.forEach { type ->
            val active = selected == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (active) BrandBlue else BaseWhite)
                    .clickable { onSelected(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    type.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) BaseWhite else OnSurfaceVariantLightColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackageDropdown(
    selected: PackageType,
    onSelected: (PackageType) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    var expanded by remember { mutableStateOf(false) }
    val display = PACKAGE_LABELS[selected] ?: selected.name

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel("Package")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = display,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = OnSurfaceVariantLightColor)
                },
                shape = FieldShape,
                colors = fieldColors,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                PackageType.entries.forEach { pkg ->
                    DropdownMenuItem(
                        text = { Text(PACKAGE_LABELS[pkg] ?: pkg.name) },
                        onClick = {
                            expanded = false
                            onSelected(pkg)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BaseWhite,
                checkedTrackColor = BrandBlue,
                uncheckedThumbColor = BaseWhite,
                uncheckedTrackColor = OutlineVariantLight,
                uncheckedBorderColor = OutlineLight
            )
        )
    }
}

@Composable
private fun TopicsInfoBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(BrandBlueTint)
            .border(1.dp, BrandBlue.copy(alpha = 0.25f), FieldShape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
        Text(
            "Each topic is copied to new enrollments as a checklist.",
            style = MaterialTheme.typography.bodySmall,
            color = BrandBlue
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicEditorCard(
    index: Int,
    topic: TopicEditorRow,
    error: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onUnitChange: (TopicDurationUnit) -> Unit,
    onRemove: () -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .border(1.dp, OutlineVariantLight, FieldShape)
            .background(OffWhite)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Topic ${index + 1}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove topic", tint = BrandRed)
            }
        }
        FormTextField(
            label = "Topic Name",
            required = true,
            value = topic.topicName,
            onValueChange = onNameChange,
            placeholder = "e.g. MS Office",
            isError = error != null,
            errorText = error,
            fieldColors = fieldColors
        )
        FormTextField(
            label = "Description",
            value = topic.description,
            onValueChange = onDescriptionChange,
            singleLine = false,
            minLines = 2,
            fieldColors = fieldColors
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.width(100.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FormLabel("Duration")
                OutlinedTextField(
                    value = if (topic.durationValue == 0) "" else topic.durationValue.toString(),
                    onValueChange = onDurationChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = FieldShape,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TopicUnitDropdown(
                selected = topic.durationUnit,
                onSelected = onUnitChange,
                fieldColors = fieldColors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicUnitDropdown(
    selected: TopicDurationUnit,
    onSelected: (TopicDurationUnit) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel("Unit")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selected.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = OnSurfaceVariantLightColor)
                },
                shape = FieldShape,
                colors = fieldColors,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                TopicDurationUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            expanded = false
                            onSelected(unit)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTopicButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(BaseWhite)
            .border(1.dp, BrandBlue.copy(alpha = 0.35f), FieldShape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text("+ Add Topic", color = BrandBlue, fontWeight = FontWeight.SemiBold)
    }
}
