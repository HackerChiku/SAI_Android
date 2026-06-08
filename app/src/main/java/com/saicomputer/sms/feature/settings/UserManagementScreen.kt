package com.saicomputer.sms.feature.settings

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
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole

private val CardShape = RoundedCornerShape(14.dp)
private val FieldShape = RoundedCornerShape(12.dp)
private val WarningOrange = Color(0xFFEA580C)
private val WarningOrangeTint = Color(0xFFFFF7ED)

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

    Column(modifier = Modifier.fillMaxSize().background(OffWhite)) {
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                ) { Text("Create", color = BrandBlue) }
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(user.email, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariantLightColor)
                    Text("Role")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                ) { Text("Save", color = BrandBlue) }
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
                ) { Text("Reset", color = BrandRed) }
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
            .clip(FieldShape)
            .background(BrandBlueTint)
            .border(1.dp, BrandBlue.copy(alpha = 0.35f), FieldShape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(6.dp))
        Text("Create User", color = BrandBlue, fontWeight = FontWeight.SemiBold)
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
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        color = OnSurfaceVariantLightColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(
                    text = user.role.name,
                    color = roleColor(user.role),
                    fontSize = 10.sp
                )
            }

            if (user.mustChangePassword) {
                MustChangePasswordBanner()
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UserActionButton(
                    label = "Edit",
                    icon = Icons.Outlined.Edit,
                    color = BrandBlue,
                    onClick = onEdit
                )
                UserActionButton(
                    label = "Reset Password",
                    icon = Icons.Outlined.VpnKey,
                    color = BrandRed,
                    onClick = onResetPassword
                )
            }
        }
    }
}

@Composable
private fun MustChangePasswordBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(WarningOrangeTint)
            .border(1.dp, WarningOrange.copy(alpha = 0.35f), FieldShape)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = WarningOrange,
            modifier = Modifier.size(18.dp)
        )
        Text(
            "Must change password on next login.",
            style = MaterialTheme.typography.bodySmall,
            color = WarningOrange,
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
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(4.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

private fun roleColor(role: UserRole) = when (role) {
    UserRole.Owner -> BrandRed
    UserRole.Admin -> StatusBlue
    UserRole.Receptionist -> StatusEmerald
}
