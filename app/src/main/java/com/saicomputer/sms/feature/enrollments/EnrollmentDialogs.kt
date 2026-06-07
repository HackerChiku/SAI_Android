package com.saicomputer.sms.feature.enrollments

import androidx.compose.foundation.layout.Arrangement
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
import com.saicomputer.sms.core.ui.BackdateToggle

@Composable
fun MarkCompleteDialog(
    incompleteTopics: List<String>,
    canBackdate: Boolean,
    onConfirm: (force: Boolean, isBackdate: Boolean, date: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var backdate by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf<String?>(null) }
    val needsForce = incompleteTopics.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (needsForce) "Incomplete topics" else "Mark Complete") },
        text = {
            Column {
                if (needsForce) {
                    Text("These topics are not complete:", style = MaterialTheme.typography.bodyMedium)
                    incompleteTopics.forEach { Text("• $it") }
                    Spacer(Modifier.height(8.dp))
                    Text("Complete anyway?")
                } else {
                    Text("Mark this enrollment as completed?")
                }
                if (canBackdate) {
                    Spacer(Modifier.height(12.dp))
                    BackdateToggle(
                        enabled = backdate,
                        onEnabledChange = { backdate = it },
                        date = date,
                        onDateChange = { date = it },
                        dateLabel = "Actual End Date"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(needsForce, backdate, date) }) {
                Text(if (needsForce) "Complete Anyway" else "Mark Complete")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CancelEnrollmentDialog(
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Enrollment") },
        text = {
            Column {
                Text("This cancels the enrollment. This cannot be undone.")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.ifBlank { null }) }) { Text("Cancel Enrollment") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep") } }
    )
}

@Composable
fun TopicCompleteDialog(
    topicName: String,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete topic") },
        text = {
            Column {
                Text(topicName)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(notes.ifBlank { null }) }) { Text("Mark complete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ExtendSubscriptionDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var months by remember { mutableStateOf(1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Extend Subscription") },
        text = {
            Column {
                Text("Extend by how many months?")
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { m ->
                        androidx.compose.material3.FilterChip(
                            selected = months == m,
                            onClick = { months = m },
                            label = { Text("$m") }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(months) }) { Text("Extend") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
