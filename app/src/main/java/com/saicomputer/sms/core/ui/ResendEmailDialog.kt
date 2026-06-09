package com.saicomputer.sms.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun ResendEmailDialog(
    defaultEmail: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf(defaultEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Resend Email") },
        text = {
            Column {
                Text("Send the document to:")
                Spacer(Modifier.height(appDimens().spacingSm))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Recipient email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(email) }, enabled = email.isNotBlank()) { Text("Send") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
