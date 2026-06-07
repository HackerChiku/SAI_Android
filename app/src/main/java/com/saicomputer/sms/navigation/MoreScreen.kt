package com.saicomputer.sms.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.core.permission.can
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
    val all = listOf(
        MoreItem(Screen.Search.route, "Search", Icons.Outlined.Search),
        MoreItem(Screen.Courses.route, "Courses", Icons.Outlined.Book),
        MoreItem(Screen.Subscriptions.route, "Subscriptions", Icons.Outlined.Autorenew, "subscriptions.list"),
        MoreItem(Screen.Payments.route, "Payments", Icons.Outlined.Description, "receipts.list"),
        MoreItem(Screen.Receipts.route, "Receipts", Icons.Outlined.Description, "receipts.list"),
        MoreItem(Screen.Certificates.route, "Certificates", Icons.Outlined.WorkspacePremium, "certificates.list"),
        MoreItem(Screen.Audit.route, "Audit Log", Icons.Outlined.History, "audit.list"),
        MoreItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings, "settings.update"),
        MoreItem(Screen.Users.route, "User Management", Icons.Outlined.Assessment, "users.create"),
        MoreItem(Screen.Exports.route, "Exports", Icons.Outlined.Download, "exports.students")
    )
    val visible = all.filter { it.permission == null || can(user, it.permission) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(visible) { item ->
            ListItem(
                headlineContent = { Text(item.label) },
                leadingContent = { Icon(item.icon, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(item.route) }
            )
            HorizontalDivider()
        }
        item {
            ListItem(
                headlineContent = { Text("Sign out", color = MaterialTheme.colorScheme.error) },
                leadingContent = {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
            )
        }
    }
}
