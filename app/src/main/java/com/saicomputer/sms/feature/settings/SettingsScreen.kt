package com.saicomputer.sms.feature.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.FormLoadingSkeleton
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.SubpageTitleBar
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstituteSettingsScreen(
    user: User? = null,
    onBack: () -> Unit,
    onOpenUsers: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { message ->
            snackbarController.show(scope, message)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubpageTitleBar(title = "Settings", onBack = onBack, user = user)

        if (state.loading) {
            FormLoadingSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
            return@Column
        }

        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = viewModel::manualRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(appDimens().spacingLg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
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
                    if (state.submitting) {
                        CircularProgressIndicator(
                            Modifier.height(appDimens().iconSizeMd),
                            strokeWidth = appDimens().spacingXxs,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Settings")
                    }
                }
                OutlinedButton(onClick = onOpenUsers, modifier = Modifier.fillMaxWidth()) {
                    Text("Manage Users")
                }
                Spacer(Modifier.height(appDimens().spacingSm))
            }
        }
    }
}
