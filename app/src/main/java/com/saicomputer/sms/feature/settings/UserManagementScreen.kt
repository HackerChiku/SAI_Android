package com.saicomputer.sms.feature.settings

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SubpageTitleBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.core.ui.theme.appDimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showCreate by remember { mutableStateOf(false) }
    var editUser by remember { mutableStateOf<User?>(null) }
    var resetUserId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SubpageTitleBar(title = "User Management", onBack = onBack, showProfile = false)

        when (val s = state) {
            is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize())
            is UiState.Error -> ErrorState(
                message = s.message,
                onRetry = viewModel::load,
                modifier = Modifier.fillMaxSize()
            )
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
                ) {
                    item {
                        CreateUserButton(onClick = { showCreate = true })
                    }

                    if (s.data.isEmpty()) {
                        item {
                            EmptyState(title = "No users yet", modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        items(s.data, key = { it.userId }) { user ->
                            UserCard(
                                user = user,
                                onEdit = { editUser = user },
                                onResetPassword = { resetUserId = user.userId }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(UserRole.Receptionist) }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Temporary password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Role")
                    Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                        UserRole.entries.forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6,
                    onClick = {
                        viewModel.createUser(name, email, password, role) {
                            snackbarController.show(scope, it)
                        }
                        showCreate = false
                    }
                ) { Text("Create", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }

    editUser?.let { user ->
        var name by remember(user.userId) { mutableStateOf(user.fullName) }
        var role by remember(user.userId) { mutableStateOf(user.role) }
        AlertDialog(
            onDismissRequest = { editUser = null },
            title = { Text("Edit User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Role")
                    Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
                        UserRole.entries.forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.updateUser(user.userId, name, role) {
                            snackbarController.show(scope, it)
                        }
                        editUser = null
                    }
                ) { Text("Save", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = { TextButton(onClick = { editUser = null }) { Text("Cancel") } }
        )
    }

    resetUserId?.let { uid ->
        var newPassword by remember(uid) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { resetUserId = null },
            title = { Text("Reset Password") },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newPassword.length >= 6,
                    onClick = {
                        viewModel.resetPassword(uid, newPassword) {
                            snackbarController.show(scope, it)
                        }
                        resetUserId = null
                    }
                ) { Text("Reset", color = MaterialTheme.colorScheme.tertiary) }
            },
            dismissButton = { TextButton(onClick = { resetUserId = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CreateUserButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(appDimens().strokeHairline, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), appDimens().fieldShape)
            .clickable(onClick = onClick)
            .padding(vertical = appDimens().spacing14),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeSm))
        Spacer(Modifier.size(appDimens().spacing6))
        Text("Create User", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onResetPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
                ) {
                    Text(
                        user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(
                    text = user.role.name,
                    color = roleColor(user.role),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (user.mustChangePassword) {
                MustChangePasswordBanner()
            }

            Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingLg)) {
                UserActionButton(
                    label = "Edit",
                    icon = Icons.Outlined.Edit,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onEdit
                )
                UserActionButton(
                    label = "Reset Password",
                    icon = Icons.Outlined.VpnKey,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = onResetPassword
                )
            }
        }
    }
}

@Composable
private fun MustChangePasswordBanner() {
    val colors = appColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(colors.warningContainer)
            .border(appDimens().strokeHairline, colors.warning.copy(alpha = 0.35f), appDimens().fieldShape)
            .padding(appDimens().spacing10),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = colors.warning,
            modifier = Modifier.size(appDimens().iconSizeSm)
        )
        Text(
            "Must change password on next login.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.warning,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UserActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(appDimens().iconSizeSm))
        Spacer(Modifier.size(appDimens().spacingXs))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun roleColor(role: UserRole) = when (role) {
    UserRole.Owner -> MaterialTheme.colorScheme.tertiary
    UserRole.Admin -> appColors().info
    UserRole.Receptionist -> appColors().success
}
