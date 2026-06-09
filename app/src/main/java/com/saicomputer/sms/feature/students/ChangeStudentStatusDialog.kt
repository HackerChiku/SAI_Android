package com.saicomputer.sms.feature.students

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus
import com.saicomputer.sms.data.model.ManualStudentStatus
import com.saicomputer.sms.core.ui.theme.appDimens

enum class StatusDialogMode { Dropout, NotTakenAdmission, Reactivate }

@Composable
fun ChangeStudentStatusDialog(
    mode: StatusDialogMode,
    enrollments: List<Enrollment>,
    onConfirm: (ManualStudentStatus, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    val ongoing = enrollments.filter { it.enrollmentStatus == EnrollmentStatus.Ongoing }

    val (title, body, confirmLabel, status) = when (mode) {
        StatusDialogMode.Dropout -> Quad(
            "Mark as Dropout",
            if (ongoing.isEmpty()) "This student will be marked as Dropout." else
                "Marking as Dropout will cancel these ongoing enrollments:",
            "Mark Dropout",
            ManualStudentStatus.Dropout
        )
        StatusDialogMode.NotTakenAdmission -> Quad(
            "Mark as Not Taken Admission",
            "This student will be marked as Not Taken Admission.",
            "Mark Not Taken",
            ManualStudentStatus.NotTakenAdmission
        )
        StatusDialogMode.Reactivate -> Quad(
            "Reactivate Student",
            "This will set the student's status back to New.",
            "Reactivate",
            ManualStudentStatus.New
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(body, style = MaterialTheme.typography.bodyMedium)
                if (mode == StatusDialogMode.Dropout && ongoing.isNotEmpty()) {
                    Spacer(Modifier.height(appDimens().spacingSm))
                    ongoing.forEach { e ->
                        Text("• ${e.courseName ?: e.courseId}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (mode != StatusDialogMode.Reactivate) {
                    Spacer(Modifier.height(appDimens().spacingMd))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(status, reason.ifBlank { null }) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
