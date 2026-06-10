package com.saicomputer.sms.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.R
import com.saicomputer.sms.core.auth.SavedAccount
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@Composable
fun LoginScreen(
    onLoggedIn: (User) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(appDimens().cornerRadiusProgress)
                .background(MaterialTheme.colorScheme.tertiary)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = appDimens().spacing28),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginLogo()
            Spacer(Modifier.height(appDimens().iconSizeMd))
            Text(
                "Sai Computer Education",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(appDimens().spacing6))
            Text(
                "Student Management System",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(appDimens().iconSizeXxl))

            when (state.step) {
                LoginStep.AccountPicker -> AccountPickerContent(
                    accounts = state.savedAccounts,
                    onSelectAccount = viewModel::selectAccount,
                    onChooseAnother = viewModel::chooseAnotherAccount
                )
                LoginStep.QuickSignIn -> QuickSignInContent(
                    account = state.selectedAccount,
                    password = state.password,
                    passwordVisible = passwordVisible,
                    submitting = state.submitting,
                    error = state.error,
                    canSubmit = state.canSubmit,
                    fieldColors = fieldColors,
                    onPasswordChange = viewModel::onPasswordChange,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    onLogin = { viewModel.login(onLoggedIn) },
                    onBack = viewModel::backToAccountPicker
                )
                LoginStep.FullSignIn -> FullSignInContent(
                    email = state.email,
                    password = state.password,
                    passwordVisible = passwordVisible,
                    submitting = state.submitting,
                    error = state.error,
                    canSubmit = state.canSubmit,
                    showBackToSaved = state.savedAccounts.isNotEmpty(),
                    fieldColors = fieldColors,
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    onLogin = { viewModel.login(onLoggedIn) },
                    onBackToSaved = viewModel::backToAccountPicker
                )
            }
        }

        Text(
            "© 2026 Sai Computer Education",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = appDimens().iconSizeMd)
        )
    }
}

@Composable
private fun AccountPickerContent(
    accounts: List<SavedAccount>,
    onSelectAccount: (SavedAccount) -> Unit,
    onChooseAnother: () -> Unit
) {
    Text(
        "Choose an account",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(appDimens().spacingMd))
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
        accounts.forEach { account ->
            SavedAccountRow(account = account, onClick = { onSelectAccount(account) })
        }
    }
    Spacer(Modifier.height(appDimens().spacingLg))
    TextButton(onClick = onChooseAnother, modifier = Modifier.fillMaxWidth()) {
        Text("Choose another account")
    }
}

@Composable
private fun SavedAccountRow(
    account: SavedAccount,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(appDimens().spacingMd),
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColoredPhotoAvatar(name = account.fullName, size = 48)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    account.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickSignInContent(
    account: SavedAccount?,
    password: String,
    passwordVisible: Boolean,
    submitting: Boolean,
    error: String?,
    canSubmit: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit
) {
    account?.let {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColoredPhotoAvatar(name = it.fullName, size = 56)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    it.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    it.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(appDimens().spacingLg))
    }

    LoginFieldLabel("Password")
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text("••••••••••") },
        singleLine = true,
        enabled = !submitting,
        shape = appDimens().fieldShape,
        colors = fieldColors,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisible) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(appDimens().iconSizeMd)
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth()
    )

    LoginErrorMessage(error)
    Spacer(Modifier.height(appDimens().spacing28))
    LoginSubmitButton(submitting = submitting, canSubmit = canSubmit, onClick = onLogin)
    Spacer(Modifier.height(appDimens().spacingSm))
    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text("Back")
    }
}

@Composable
private fun FullSignInContent(
    email: String,
    password: String,
    passwordVisible: Boolean,
    submitting: Boolean,
    error: String?,
    canSubmit: Boolean,
    showBackToSaved: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisible: () -> Unit,
    onLogin: () -> Unit,
    onBackToSaved: () -> Unit
) {
    LoginFieldLabel("Email")
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = { Text("you@saicomputer.in") },
        singleLine = true,
        enabled = !submitting,
        shape = appDimens().fieldShape,
        colors = fieldColors,
        trailingIcon = {
            Icon(
                Icons.Outlined.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(appDimens().iconSizeMd)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(appDimens().iconSizeSm))
    LoginFieldLabel("Password")
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text("••••••••••") },
        singleLine = true,
        enabled = !submitting,
        shape = appDimens().fieldShape,
        colors = fieldColors,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisible) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Outlined.VisibilityOff
                    } else {
                        Icons.Outlined.Visibility
                    },
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(appDimens().iconSizeMd)
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth()
    )

    LoginErrorMessage(error)
    Spacer(Modifier.height(appDimens().spacing28))
    LoginSubmitButton(submitting = submitting, canSubmit = canSubmit, onClick = onLogin)

    if (showBackToSaved) {
        Spacer(Modifier.height(appDimens().spacingSm))
        TextButton(onClick = onBackToSaved, modifier = Modifier.fillMaxWidth()) {
            Text("Back to saved accounts")
        }
    }
}

@Composable
private fun LoginErrorMessage(error: String?) {
    if (error != null) {
        Spacer(Modifier.height(appDimens().spacingMd))
        Text(
            error,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LoginSubmitButton(
    submitting: Boolean,
    canSubmit: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = canSubmit,
        shape = appDimens().fieldShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
            disabledContentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(appDimens().iconSizeListBoxLg)
    ) {
        if (submitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(appDimens().iconSizeListInner),
                strokeWidth = appDimens().spacingXxs,
                color = MaterialTheme.colorScheme.surface
            )
        } else {
            Text(
                "Sign in",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginLogo() {
    Image(
        painter = painterResource(R.drawable.sai_logo),
        contentDescription = "Sai Computer Education logo",
        modifier = Modifier
            .size(appDimens().loginLogoSize)
            .clip(appDimens().logoShape)
    )
}

@Composable
private fun LoginFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = appDimens().spacingSm)
    )
}
