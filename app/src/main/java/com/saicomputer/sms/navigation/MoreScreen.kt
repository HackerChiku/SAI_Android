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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Autorenew
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
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.data.model.User

private data class MoreItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val permission: String? = null
)

@Composable
fun MoreScreen(
    user: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val menuItems = listOf(
        MoreItem(Screen.Courses.route, "Courses", Icons.AutoMirrored.Outlined.MenuBook, "courses.list"),
        MoreItem(Screen.Subscriptions.route, "Subscriptions", Icons.Outlined.Autorenew, "subscriptions.list"),
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
                .background(OffWhite)
        ) {
            items(visible) { item ->
                MoreMenuRow(
                    label = item.label,
                    icon = item.icon,
                    onClick = { onNavigate(item.route) }
                )
                HorizontalDivider(color = OutlineVariantLight)
            }
            if (showUserManagement) {
                item {
                    MoreMenuRow(
                        label = "User Management",
                        icon = Icons.Outlined.Assessment,
                        onClick = { onNavigate(Screen.Users.route) }
                    )
                    HorizontalDivider(color = OutlineVariantLight)
                }
            }
            item {
                MoreMenuRow(
                    label = "Sign out",
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    labelColor = BrandRed,
                    iconTint = BrandRed,
                    showChevron = false,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun MoreHeader(user: User?) {
    val initial = user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "More",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BaseWhite
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandBlue.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = BaseWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Text(
            "Sai Computer Education",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BaseWhite,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Student Management System",
            style = MaterialTheme.typography.bodyMedium,
            color = BaseWhite.copy(alpha = 0.75f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun MoreMenuRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = OnSurfaceVariantLightColor,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BaseWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
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
                tint = OnSurfaceVariantLightColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
