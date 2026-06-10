package com.saicomputer.sms.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ProfileMenuButton
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens

private data class MoreItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val permission: String? = null
)

@Composable
fun MoreScreen(
    user: User?,
    onNavigate: (String) -> Unit
) {
    val menuItems = listOf(
        MoreItem(Screen.Courses.route, "Courses", Icons.AutoMirrored.Outlined.MenuBook, "courses.list"),
        MoreItem(Screen.Receipts.route, "Receipts", Icons.AutoMirrored.Outlined.ReceiptLong, "receipts.list"),
        MoreItem(Screen.Certificates.route, "Certificates", Icons.Outlined.WorkspacePremium, "certificates.list"),
        MoreItem(Screen.Audit.route, "Audit Log", Icons.Outlined.History, "audit.list"),
        MoreItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings, "settings.update"),
        MoreItem(Screen.Exports.route, "Exports", Icons.Outlined.Download, "exports.students")
    )
    val visible = menuItems.filter { it.permission == null || can(user, it.permission) }
    val showUserManagement = can(user, "users.create")

    Column(modifier = Modifier.fillMaxSize()) {
        MoreHeader(user = user)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(visible) { item ->
                MoreMenuRow(
                    label = item.label,
                    icon = item.icon,
                    onClick = { onNavigate(item.route) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (showUserManagement) {
                item {
                    MoreMenuRow(
                        label = "User Management",
                        icon = Icons.Outlined.Assessment,
                        onClick = { onNavigate(Screen.Users.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun MoreHeader(user: User?) {
    AppTopBarBox {
        Column(modifier = Modifier.fillMaxWidth()) {
            AppTitleBarRow(
                leading = {
                    Text(
                        "More",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                },
                actions = {
                    ProfileMenuButton(user = user, size = appDimens().callButtonSize)
                }
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = appDimens().iconSizeMd)
                    .padding(bottom = appDimens().iconSizeMd)
            ) {
                Text(
                    "Sai Computer Education",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = appDimens().spacingLg)
                )
                Text(
                    "Student Management System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = appDimens().spacingXs)
                )
            }
        }
    }
}

@Composable
private fun MoreMenuRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().iconSizeSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(appDimens().iconSizeLg)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(appDimens().iconSizeListInner)
            )
        }
    }
}
