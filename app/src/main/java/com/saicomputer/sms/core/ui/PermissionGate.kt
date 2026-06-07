package com.saicomputer.sms.core.ui

import androidx.compose.runtime.Composable
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.data.model.User

/**
 * Renders [content] only when [user] may perform [action]. UX-only gate; the
 * server still enforces permissions.
 */
@Composable
fun PermissionGate(
    user: User?,
    action: String,
    content: @Composable () -> Unit
) {
    if (can(user, action)) content()
}
