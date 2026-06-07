package com.saicomputer.sms.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChangePasswordScreen(
    onChanged: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Change your password", style = MaterialTheme.typography.titleLarge)
        Text(
            "You must set a new password before continuing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.currentPassword,
            onValueChange = viewModel::onCurrentChange,
            label = { Text("Current password") },
            singleLine = true,
            enabled = !state.submitting,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.newPassword,
            onValueChange = viewModel::onNewChange,
            label = { Text("New password") },
            singleLine = true,
            enabled = !state.submitting,
            isError = state.newPasswordError != null,
            supportingText = state.newPasswordError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmChange,
            label = { Text("Confirm new password") },
            singleLine = true,
            enabled = !state.submitting,
            isError = state.confirmError != null,
            supportingText = state.confirmError?.let { { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.submit(onChanged) },
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
                Text("Update password")
            }
        }
    }
}
