package com.saicomputer.sms.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.TitleBarBackButton
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
        AppTopBarBox {
            AppTitleBarRow(
                leading = {
                    if (!forced && onBack != null) {
                        TitleBarBackButton(onBack = onBack)
                    }
                    Text(
                        "Change Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(appDimens().spacingLg)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = appDimens().cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
            ) {
                Column(
                    modifier = Modifier.padding(appDimens().spacingLg),
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
                ) {
                    Text(
                        if (forced) {
                            "You must set a new password before continuing."
                        } else {
                            "Enter your current password and choose a new one."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = state.currentPassword,
                        onValueChange = viewModel::onCurrentChange,
                        label = { Text("Current password") },
                        singleLine = true,
                        enabled = !state.submitting,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }

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
    }
}
