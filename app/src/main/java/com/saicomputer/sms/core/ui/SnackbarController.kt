package com.saicomputer.sms.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Thin wrapper over the app-level [SnackbarHostState] so any screen can post
 * success / error feedback without owning a host.
 */
class SnackbarController(
    private val hostState: SnackbarHostState
) {
    fun show(scope: CoroutineScope, message: String) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }
}
