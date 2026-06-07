package com.saicomputer.sms.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Scaffold with bottom navigation used by top-level destinations. Items are
 * filtered by role/permission (UX-only).
 */
@Composable
fun MainShell(
    navController: NavHostController,
    user: User?,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val items = buildList {
        if (user?.role != UserRole.Receptionist && can(user, "dashboard.summary")) {
            add(BottomItem(Screen.Dashboard.route, "Dashboard", Icons.Outlined.Dashboard))
        }
        add(BottomItem(Screen.Students.route, "Students", Icons.Outlined.People))
        if (can(user, "courses.list")) {
            add(BottomItem(Screen.Courses.route, "Courses", Icons.AutoMirrored.Outlined.MenuBook))
        }
        add(BottomItem(Screen.More.route, "More", Icons.Outlined.MoreHoriz))
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}
