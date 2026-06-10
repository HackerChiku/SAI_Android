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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.UnfoldMore
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.REGISTRATION_SESSION_LABELS
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.BackdateEntryCard
import com.saicomputer.sms.core.ui.TitleBarBackButton
import com.saicomputer.sms.core.ui.FormLoadingSkeleton
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.theme.appColors
import com.saicomputer.sms.data.model.Gender
import com.saicomputer.sms.data.model.RegistrationSession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.saicomputer.sms.core.ui.theme.appDimens


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
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        StudentFormHeader(
            title = if (state.isEdit) "Edit Student" else "New Student",
            onBack = onBack
        )

        if (state.loading) {
            FormLoadingSkeleton(Modifier.fillMaxSize())
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
        ) {
            if (!state.isEdit && can(user, "system.backdate")) {
                BackdateEntryCard(
                    enabled = state.backdateEnabled,
                    onEnabledChange = { v -> viewModel.update { it.copy(backdateEnabled = v) } },
                    date = state.effectiveCreatedAt,
                    onDateChange = { d -> viewModel.update { it.copy(effectiveCreatedAt = d) } }
                )
            }

            FormSectionCard(title = "Registration Session", compact = true) {
                RegistrationSessionSelector(
                    selected = state.registrationSession,
                    onSelected = viewModel::onRegistrationSessionChange
                )
                if (state.registrationSession != RegistrationSession.NewRecord) {
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
                Spacer(Modifier.height(appDimens().spacingMd))
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
                    modifier = Modifier.padding(horizontal = appDimens().spacingXs)
                )
            }

            Spacer(Modifier.height(appDimens().spacingMd))
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
                    .padding(vertical = appDimens().spacingMd),
                shape = appDimens().fieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(appDimens().iconSizeListInner),
                        strokeWidth = appDimens().spacingXxs,
                        color = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Text(
                        if (state.isEdit) "Save Changes" else "Create Student",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = appDimens().spacingXs)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentFormHeader(title: String, onBack: () -> Unit) {
    AppTopBarBox {
        AppTitleBarRow(
            leading = {
                TitleBarBackButton(onBack = onBack)
                Text(
                    title,
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
private fun FormSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    content: @Composable () -> Unit
) {
    val padding = if (compact) appDimens().spacingMd else appDimens().spacingLg
    val spacing = if (compact) appDimens().spacingSm else appDimens().spacingMd
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Text(
                title,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun RegistrationSessionSelector(
    selected: RegistrationSession,
    onSelected: (RegistrationSession) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .border(appDimens().strokeHairline, MaterialTheme.colorScheme.outlineVariant, appDimens().fieldShape)
    ) {
        RegistrationSession.entries.forEach { session ->
            val active = selected == session
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .clickable { onSelected(session) }
                    .padding(vertical = appDimens().spacingSm, horizontal = appDimens().spacingXxs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    REGISTRATION_SESSION_LABELS[session] ?: session.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FormLabel(label: String, required: Boolean = false) {
    Text(
        buildAnnotatedString {
            append(label)
            if (required) {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.tertiary)) { append(" *") }
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
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
            shape = appDimens().fieldShape,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        when {
            isError && errorText != null ->
                Text(errorText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            helperText != null ->
                Text(helperText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
        FormLabel(label)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatDobDisplay(value),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("dd / mm / yyyy") },
                trailingIcon = {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = appDimens().fieldShape,
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

    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
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
                    Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                shape = appDimens().fieldShape,
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
private fun AadhaarInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, appDimens().fieldShape)
            .border(appDimens().strokeHairline, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), appDimens().fieldShape)
            .padding(appDimens().spacingMd),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(appDimens().iconSizeMd)
        )
        Text(
            "Aadhaar is masked on all screens. Only authorized roles can view the document.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DocumentUploadZone(
    label: String,
    fileLabel: String?,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
        FormLabel(label)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = appDimens().formMinHeight)
                .dashedBorder(
                    MaterialTheme.colorScheme.outlineVariant,
                    appDimens().strokeDashed,
                    appDimens().spacingMd
                )
                .background(MaterialTheme.colorScheme.background, appDimens().fieldShape)
                .clickable(onClick = onClick)
                .padding(vertical = appDimens().iconSizeLg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                Icon(
                    Icons.Outlined.FileUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(appDimens().spacing32)
                )
                Text(
                    fileLabel ?: "Tap to upload",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Modifier.dashedBorder(color: androidx.compose.ui.graphics.Color, strokeWidth: Dp, cornerRadius: Dp): Modifier =
    this.drawBehind {
        val strokeWidthPx = strokeWidth.toPx()
        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        val corner = cornerRadius.toPx()
        drawRoundRect(
            color = color,
            style = Stroke(width = strokeWidthPx, pathEffect = dash),
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
