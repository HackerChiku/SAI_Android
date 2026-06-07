package com.saicomputer.sms.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController

@Composable
fun InstituteSettingsScreen(
    onBack: () -> Unit,
    onOpenUsers: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { SmsTopBar(title = "Settings", onBack = onBack) }) { padding ->
        if (state.loading) {
            Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.padding(16.dp))
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.rows.forEach { row ->
                OutlinedTextField(
                    value = row.value,
                    onValueChange = { v -> viewModel.updateValue(row.key, v) },
                    label = { Text(row.key) },
                    supportingText = row.usedIn?.takeIf { it.isNotBlank() }?.let { { Text("Used in: $it") } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)

            Button(
                onClick = { viewModel.save { snackbarController.show(scope, it) } },
                enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.submitting) CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Save Settings")
            }
            OutlinedButton(onClick = onOpenUsers, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Users")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
