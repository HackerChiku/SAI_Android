package com.saicomputer.sms.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun ChangePasswordScreen(
    onChanged: () -> Unit,
    forced: Boolean = true,
    onBack: (() -> Unit)? = null,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        if (!forced && onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = appDimens().spacingXs, vertical = appDimens().spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.surface)
                }
                Text(
                    "Change Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(appDimens().iconSizeLg),
            verticalArrangement = Arrangement.Center
        ) {
        if (forced) {
            Text("Change your password", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            if (forced) {
                "You must set a new password before continuing."
            } else {
                "Enter your current password and choose a new one."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(appDimens().iconSizeLg))

        OutlinedTextField(
            value = state.currentPassword,
            onValueChange = viewModel::onCurrentChange,
            label = { Text("Current password") },
            singleLine = true,
            enabled = !state.submitting,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(appDimens().spacingMd))
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
        Spacer(Modifier.height(appDimens().spacingMd))
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
            Spacer(Modifier.height(appDimens().spacingMd))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(appDimens().iconSizeLg))
        Button(
            onClick = { viewModel.submit(onChanged) },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(appDimens().iconSizeMd),
                    strokeWidth = appDimens().spacingXxs,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Update password")
            }
        }
        }
    }
}
