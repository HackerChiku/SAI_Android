package com.saicomputer.sms.core.ui

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.core.ui.theme.appDimens

data class UserMenuActions(
    val onProfile: () -> Unit,
    val onLogout: () -> Unit
)

val LocalUserMenuActions = compositionLocalOf<UserMenuActions?> { null }

@Composable
fun ProvideUserMenuActions(
    actions: UserMenuActions,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalUserMenuActions provides actions, content = content)
}

@Composable
fun ProfileMenuButton(
    user: User?,
    modifier: Modifier = Modifier,
    size: Dp = appDimens().iconSizeXxl,
    backgroundAlpha: Float = 0.5f
) {
    val actions = LocalUserMenuActions.current
    var expanded by remember { mutableStateOf(false) }
    val initial = user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(size)
                .border(appDimens().spacingXxs, MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                initial,
                color = MaterialTheme.colorScheme.surface,
                fontWeight = FontWeight.Bold,
                style = if (size >= appDimens().callButtonSize) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleSmall
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(appDimens().dropdownMenuWidth),
            shape = appDimens().cardShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacing14)) {
                Text(
                    user?.fullName ?: "Signed in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    user?.email.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = appDimens().spacingXxs)
                )
                user?.role?.let { role ->
                    Pill(
                        text = roleLabel(role),
                        color = appColors().info,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = appDimens().spacingSm)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ProfileMenuRow(
                label = "Profile",
                icon = Icons.Outlined.Person,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    expanded = false
                    actions?.onProfile?.invoke()
                }
            )
            ProfileMenuRow(
                label = "Sign out",
                icon = Icons.AutoMirrored.Outlined.Logout,
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    expanded = false
                    actions?.onLogout?.invoke()
                }
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(appDimens().iconSizeMd))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.Owner -> "Owner"
    UserRole.Admin -> "Admin"
    UserRole.Receptionist -> "Receptionist"
}
