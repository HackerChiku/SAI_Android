package com.saicomputer.sms.feature.students

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.REGISTRATION_SESSION_LABELS
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineLight
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.data.model.Gender
import com.saicomputer.sms.data.model.RegistrationSession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val FieldShape = RoundedCornerShape(12.dp)
private val CardShape = RoundedCornerShape(14.dp)
private val BackdateOrange = Color(0xFFEA580C)
private val BackdateOrangeTint = Color(0xFFFFF7ED)

private val GENDER_LABELS = mapOf(
    Gender.Male to "Male",
    Gender.Female to "Female",
    Gender.Other to "Other",
    Gender.PreferNotToSay to "Prefer not to say"
)

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
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val label = uri?.let { runCatching { context.contentResolver.query(it, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        } }.getOrNull() } ?: uri?.lastPathSegment
        viewModel.onPhotoPicked(uri, label)
    }
    val aadhaarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val label = uri?.let { runCatching { context.contentResolver.query(it, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        } }.getOrNull() } ?: uri?.lastPathSegment
        viewModel.onAadhaarPicked(uri, label)
    }

    LaunchedEffect(studentId) { viewModel.initialize(studentId) }

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
        StudentFormHeader(
            title = if (state.isEdit) "Edit Student" else "New Student",
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
            if (!state.isEdit && can(user, "system.backdate")) {
                BackdateEntryCard(
                    enabled = state.backdateEnabled,
                    onEnabledChange = { v -> viewModel.update { it.copy(backdateEnabled = v) } },
                    date = state.effectiveCreatedAt,
                    onDateChange = { d -> viewModel.update { it.copy(effectiveCreatedAt = d) } }
                )
            }

            FormSectionCard(title = "Registration Session") {
                RegistrationSession.entries.forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.registrationSession == session,
                                onClick = { viewModel.onRegistrationSessionChange(session) }
                            )
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.registrationSession == session,
                            onClick = { viewModel.onRegistrationSessionChange(session) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = BrandRed,
                                unselectedColor = OnSurfaceVariantLightColor
                            )
                        )
                        Text(
                            REGISTRATION_SESSION_LABELS[session] ?: session.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                if (state.registrationSession != RegistrationSession.NewRecord) {
                    Spacer(Modifier.height(8.dp))
                    FormTextField(
                        label = "Old Registration Number",
                        required = true,
                        value = state.oldRegistrationNumber,
                        onValueChange = { v -> viewModel.update { it.copy(oldRegistrationNumber = v) } },
                        isError = state.oldRegError != null,
                        errorText = state.oldRegError,
                        fieldColors = fieldColors
                    )
                }
            }

            FormSectionCard(title = "Basic Info") {
                FormTextField(
                    label = "Full Name",
                    required = true,
                    value = state.fullName,
                    onValueChange = { v -> viewModel.update { it.copy(fullName = v) } },
                    placeholder = "e.g. Priya Sharma",
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Phone",
                    required = true,
                    value = state.phoneNumber,
                    onValueChange = { v -> viewModel.update { it.copy(phoneNumber = v.filter { c -> c.isDigit() }.take(10)) } },
                    placeholder = "98765 43210",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = state.phoneError != null,
                    errorText = state.phoneError,
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Email",
                    value = state.email,
                    onValueChange = { v -> viewModel.update { it.copy(email = v) } },
                    placeholder = "priya@example.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = state.emailError != null,
                    errorText = state.emailError,
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Parent / Guardian Name",
                    value = state.parentName,
                    onValueChange = { v -> viewModel.update { it.copy(parentName = v) } },
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Parent Phone",
                    value = state.parentPhone,
                    onValueChange = { v -> viewModel.update { it.copy(parentPhone = v.filter { c -> c.isDigit() }.take(10)) } },
                    placeholder = "98700 11223",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    fieldColors = fieldColors
                )
                FormDateField(
                    label = "Date of Birth",
                    value = state.dateOfBirth,
                    onValueChange = viewModel::onDateOfBirthChange,
                    fieldColors = fieldColors
                )
                GenderDropdown(
                    selected = state.gender,
                    onSelected = viewModel::onGenderChange,
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Address",
                    value = state.address,
                    onValueChange = { v -> viewModel.update { it.copy(address = v) } },
                    singleLine = false,
                    minLines = 2,
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Qualification",
                    value = state.academicQualification,
                    onValueChange = { v -> viewModel.update { it.copy(academicQualification = v) } },
                    placeholder = "e.g. HSC, Graduation",
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Last Institution",
                    value = state.lastInstitution,
                    onValueChange = { v -> viewModel.update { it.copy(lastInstitution = v) } },
                    fieldColors = fieldColors
                )
                FormTextField(
                    label = "Notes",
                    value = state.additionalNotes,
                    onValueChange = { v -> viewModel.update { it.copy(additionalNotes = v) } },
                    placeholder = "Any additional notes...",
                    singleLine = false,
                    minLines = 3,
                    fieldColors = fieldColors
                )
            }

            FormSectionCard(title = "Aadhaar Details") {
                FormTextField(
                    label = "Aadhaar Number",
                    value = if (state.aadhaarLoading) "" else state.aadhaarNumber,
                    onValueChange = viewModel::onAadhaarChange,
                    placeholder = "12-digit number",
                    enabled = !state.aadhaarLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.aadhaarError != null,
                    errorText = state.aadhaarError,
                    helperText = "Stored securely. Only last 4 digits displayed.",
                    fieldColors = fieldColors
                )
                AadhaarInfoBanner()
            }

            FormSectionCard(title = "Documents & Photo") {
                DocumentUploadZone(
                    label = "Student Photo",
                    fileLabel = state.photoFileLabel,
                    onClick = { photoPicker.launch("image/*") }
                )
                Spacer(Modifier.height(12.dp))
                DocumentUploadZone(
                    label = "Aadhaar Document Photo",
                    fileLabel = state.aadhaarFileLabel,
                    onClick = { aadhaarPicker.launch("image/*") }
                )
            }

            if (state.error != null) {
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
        }

        Button(
            onClick = {
                viewModel.submit { id ->
                    snackbarController.show(scope, if (state.isEdit) "Student updated" else "Student created")
                    onSaved(id)
                }
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
                    if (state.isEdit) "Save Changes" else "Create Student",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StudentFormHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = BaseWhite
            )
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
    enabled: Boolean = true,
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
            enabled = enabled,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDateField(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel(label)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatDobDisplay(value),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("dd / mm / yyyy") },
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = OnSurfaceVariantLightColor)
                },
                shape = FieldShape,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showPicker = true }
            )
        }
    }

    if (showPicker) {
        val initialMillis = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = LocalDate.now(Formatters.IST)
                        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    return utcTimeMillis <= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(date.toString())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    selected: Gender?,
    onSelected: (Gender?) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    var expanded by remember { mutableStateOf(false) }
    val display = selected?.let { GENDER_LABELS[it] } ?: "Select gender"

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FormLabel("Gender")
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
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Select gender") },
                    onClick = { expanded = false; onSelected(null) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
                Gender.entries.forEach { gender ->
                    DropdownMenuItem(
                        text = { Text(GENDER_LABELS[gender] ?: gender.name) },
                        onClick = { expanded = false; onSelected(gender) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
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
            .dashedBorder(BackdateOrange.copy(alpha = 0.65f), FieldShape)
            .background(BackdateOrangeTint, FieldShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = BackdateOrange,
                modifier = Modifier.size(26.dp)
            )
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
                    uncheckedTrackColor = OutlineVariantLight,
                    uncheckedBorderColor = OutlineLight
                )
            )
        }
        if (enabled) {
            BackdateDateField(
                value = date,
                onValueChange = onDateChange
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = BackdateOrange,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "No confirmation email will be sent automatically. Receipt PDF will still be generated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BackdateOrange,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackdateDateField(
    value: String?,
    onValueChange: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BackdateOrange,
        unfocusedBorderColor = BackdateOrange.copy(alpha = 0.7f),
        focusedContainerColor = BackdateOrangeTint,
        unfocusedContainerColor = BackdateOrangeTint,
        focusedTextColor = BackdateOrange,
        unfocusedTextColor = BackdateOrange
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatBackdateDisplay(value),
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text("dd/mm/yyyy", color = BackdateOrange.copy(alpha = 0.5f))
            },
            trailingIcon = {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = BackdateOrange)
            },
            shape = FieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showPicker = true }
        )
    }

    if (showPicker) {
        val initialMillis = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = LocalDate.now(Formatters.IST)
                        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    return utcTimeMillis <= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(picked.toString())
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun AadhaarInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlueTint, FieldShape)
            .border(1.dp, BrandBlue.copy(alpha = 0.2f), FieldShape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(20.dp)
        )
        Text(
            "Aadhaar is masked on all screens. Only authorized roles can view the document.",
            style = MaterialTheme.typography.bodySmall,
            color = BrandBlue
        )
    }
}

@Composable
private fun DocumentUploadZone(
    label: String,
    fileLabel: String?,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormLabel(label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .dashedBorder(OutlineVariantLight, FieldShape)
                .background(OffWhite, FieldShape)
                .clickable(onClick = onClick)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Outlined.FileUpload,
                    contentDescription = null,
                    tint = OnSurfaceVariantLightColor,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    fileLabel ?: "Tap to upload",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariantLightColor
                )
            }
        }
    }
}

private fun Modifier.dashedBorder(color: androidx.compose.ui.graphics.Color, shape: RoundedCornerShape): Modifier =
    this.drawBehind {
        val strokeWidth = 1.5.dp.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        val corner = 12.dp.toPx()
        drawRoundRect(
            color = color,
            style = Stroke(width = strokeWidth, pathEffect = dash),
            cornerRadius = CornerRadius(corner, corner)
        )
    }

private fun formatDobDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val d = LocalDate.parse(iso)
        String.format("%02d / %02d / %04d", d.dayOfMonth, d.monthValue, d.year)
    }.getOrDefault("")
}

private fun formatBackdateDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return runCatching {
        val d = LocalDate.parse(iso)
        String.format("%02d/%02d/%04d", d.dayOfMonth, d.monthValue, d.year)
    }.getOrDefault("")
}
