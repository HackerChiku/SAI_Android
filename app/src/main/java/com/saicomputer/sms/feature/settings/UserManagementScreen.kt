package com.saicomputer.sms.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.model.UserRole

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
    var resetUserId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { SmsTopBar(title = "User Management", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = "Add user")
            }
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> LoadingSkeleton(Modifier.fillMaxSize().padding(padding))
            is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load, modifier = Modifier.fillMaxSize().padding(padding))
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(s.data) { u ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(u.fullName, fontWeight = FontWeight.SemiBold)
                                    Text(u.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    GenericBadge(u.role.name)
                                }
                                TextButton(onClick = { resetUserId = u.userId }) {
                                    Text("Reset Password")
                                }
                            }
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
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
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
                            FilterChip(selected = role == r, onClick = { role = r }, label = { Text(r.name) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6,
                    onClick = {
                        viewModel.createUser(name, email, password, role) { snackbarController.show(scope, it) }
                        showCreate = false
                    }
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
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
                        viewModel.resetPassword(uid, newPassword) { snackbarController.show(scope, it) }
                        resetUserId = null
                    }
                ) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { resetUserId = null }) { Text("Cancel") } }
        )
    }
}
