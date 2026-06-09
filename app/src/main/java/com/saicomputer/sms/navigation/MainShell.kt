package com.saicomputer.sms.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.core.ui.theme.appDimens

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainShell(
    navController: NavHostController,
    user: User?,
    currentRoute: String,
    showBottomBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val items = buildBottomNavItems(user)

    fun navigateTab(route: String) {
        if (!isRouteSelected(currentRoute, route)) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar && items.isNotEmpty()) {
                BottomNavBar(
                    items = items,
                    currentRoute = currentRoute,
                    onItemClick = ::navigateTab
                )
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

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        HorizontalDividerCompat()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(appDimens().bottomNavHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = isRouteSelected(currentRoute, item.route)
                BottomNavItemView(
                    item = item,
                    selected = selected,
                    onClick = { onItemClick(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = appDimens().spacing6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = color,
            modifier = Modifier.size(appDimens().iconSizeLg)
        )
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
        Box(
            modifier = Modifier
                .size(width = appDimens().spacing28, height = appDimens().cornerRadiusProgress)
                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
private fun HorizontalDividerCompat() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(appDimens().strokeHairline)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

fun shouldShowBottomBar(route: String?, user: User?): Boolean {
    if (user == null || route.isNullOrBlank()) return false
    return route != Screen.Login.route && route != Screen.ChangePassword.route
}

private fun isRouteSelected(currentRoute: String, itemRoute: String): Boolean {
    return when (itemRoute) {
        Screen.Dashboard.route -> currentRoute == Screen.Dashboard.route

        Screen.Students.route -> currentRoute == Screen.Students.route ||
            currentRoute == Screen.StudentNew.route ||
            currentRoute == Screen.StudentDetail.route ||
            currentRoute == Screen.StudentEdit.route ||
            currentRoute == Screen.Search.route ||
            currentRoute.startsWith("student/") ||
            isStudentEnrollmentRoute(currentRoute)

        Screen.Enrollments.route -> isEnrollTabRoute(currentRoute)

        Screen.Payments.route -> currentRoute == Screen.Payments.route ||
            currentRoute == Screen.PaymentNew.route ||
            currentRoute.startsWith("payment_new")

        Screen.More.route -> isMoreTabRoute(currentRoute)

        else -> currentRoute == itemRoute || currentRoute.startsWith("$itemRoute?")
    }
}

private fun isStudentEnrollmentRoute(route: String): Boolean {
    if (route != Screen.EnrollmentNew.route && !route.startsWith("enrollment_new")) return false
    val studentId = route.substringAfter("studentId=", "").substringBefore("&")
    return studentId.isNotBlank()
}

private fun isEnrollTabRoute(route: String): Boolean {
    if (route == Screen.Enrollments.route) return true
    if (route == Screen.EnrollmentDetail.route || route.startsWith("enrollment/")) return true
    if (route != Screen.EnrollmentNew.route && !route.startsWith("enrollment_new")) return false
    val studentId = route.substringAfter("studentId=", "").substringBefore("&")
    return studentId.isBlank()
}

private fun isMoreTabRoute(route: String): Boolean {
    if (route == Screen.More.route) return true
    return route == Screen.Courses.route ||
        route == Screen.CourseNew.route ||
        route == Screen.CourseDetail.route ||
        route == Screen.CourseEdit.route ||
        route.startsWith("course") ||
        route == Screen.Subscriptions.route ||
        route == Screen.Receipts.route ||
        route == Screen.Certificates.route ||
        route == Screen.Audit.route ||
        route == Screen.Settings.route ||
        route == Screen.Users.route ||
        route == Screen.Exports.route ||
        route == Screen.Profile.route
}

private fun buildBottomNavItems(user: User?): List<BottomNavItem> {
    val isReceptionist = user?.role == UserRole.Receptionist
    return buildList {
        if (!isReceptionist && can(user, "dashboard.summary")) {
            add(BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Outlined.Dashboard))
        }
        add(BottomNavItem(Screen.Students.route, "Students", Icons.Outlined.People))
        add(
            BottomNavItem(
                route = Screen.Enrollments.route,
                label = "Enroll",
                icon = Icons.AutoMirrored.Outlined.Assignment
            )
        )
        if (can(user, "receipts.list")) {
            add(BottomNavItem(Screen.Payments.route, "Payments", Icons.Outlined.CreditCard))
        }
        add(BottomNavItem(Screen.More.route, "More", Icons.Outlined.Apps))
    }
}
