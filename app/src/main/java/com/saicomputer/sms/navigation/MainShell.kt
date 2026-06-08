package com.saicomputer.sms.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.saicomputer.sms.R
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import kotlinx.coroutines.launch

private data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val permission: String? = null,
    val hideForReceptionist: Boolean = false
)

private data class DrawerSection(val title: String, val items: List<DrawerItem>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    navController: NavHostController,
    user: User?,
    currentRoute: String,
    title: String,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sections = buildDrawerSections(user)

    fun navigateFromDrawer(route: String) {
        scope.launch { drawerState.close() }
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.sai_logo),
                                contentDescription = "Sai Computer Education logo",
                                modifier = Modifier.size(56.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Sai Computer Education",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Student Management",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        user?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                it.fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                it.role.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    sections.forEach { section ->
                        Text(
                            section.title,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        section.items.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = { navigateFromDrawer(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        label = {
                            Text("Sign out", color = MaterialTheme.colorScheme.error)
                        },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Sign out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            title,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Open menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
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
}

private fun buildDrawerSections(user: User?): List<DrawerSection> {
    val isReceptionist = user?.role == UserRole.Receptionist

    val main = buildList {
        if (!isReceptionist && can(user, "dashboard.summary")) {
            add(DrawerItem(Screen.Dashboard.route, "Dashboard", Icons.Outlined.Dashboard))
        }
        add(DrawerItem(Screen.Students.route, "Students", Icons.Outlined.People))
        if (can(user, "courses.list")) {
            add(DrawerItem(Screen.Courses.route, "Courses", Icons.AutoMirrored.Outlined.MenuBook))
        }
    }

    val billing = buildList {
        add(
            DrawerItem(
                Screen.Receipts.route,
                "Receipts",
                Icons.Outlined.Description,
                "receipts.list"
            )
        )
        add(
            DrawerItem(
                Screen.Certificates.route,
                "Certificates",
                Icons.Outlined.WorkspacePremium,
                "certificates.list"
            )
        )
    }.filter { it.permission == null || can(user, it.permission) }

    val admin = buildList {
        add(DrawerItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings, "settings.update"))
        add(
            DrawerItem(
                Screen.Users.route,
                "User Management",
                Icons.Outlined.Assessment,
                "users.create"
            )
        )
    }.filter { it.permission == null || can(user, it.permission) }

    return buildList {
        if (main.isNotEmpty()) add(DrawerSection("Main", main))
        if (billing.isNotEmpty()) add(DrawerSection("Billing", billing))
        if (admin.isNotEmpty()) add(DrawerSection("Administration", admin))
    }
}
