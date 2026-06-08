package com.saicomputer.sms.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val TAB_ROUTE_PREFIXES = listOf(
    Screen.Dashboard.route,
    Screen.Students.route,
    "enrollment_new",
    Screen.Receipts.route,
    Screen.Payments.route,
    Screen.More.route
)

@Composable
fun MainShell(
    navController: NavHostController,
    user: User?,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val items = buildBottomNavItems(user)
    val showBottomNav = isTabRoute(currentRoute)

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
        bottomBar = {
            if (showBottomNav && items.isNotEmpty()) {
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
    Column(modifier = Modifier.fillMaxWidth().background(BaseWhite)) {
        HorizontalDividerCompat()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
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
    val color = if (selected) BrandRed else OnSurfaceVariantLightColor

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 3.dp)
                .background(if (selected) BrandRed else BaseWhite)
        )
    }
}

@Composable
private fun HorizontalDividerCompat() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(OutlineVariantLight)
    )
}

private fun isTabRoute(route: String): Boolean {
    if (route.startsWith("enrollment_new")) {
        val studentId = route.substringAfter("studentId=", "").substringBefore("&")
        return studentId.isBlank()
    }
    return TAB_ROUTE_PREFIXES.any { route == it || route.startsWith("$it?") }
}

private fun isRouteSelected(currentRoute: String, itemRoute: String): Boolean {
    if (itemRoute == Screen.Payments.route) {
        return currentRoute == Screen.Payments.route
    }
    if (itemRoute.startsWith("enrollment_new")) {
        return currentRoute.startsWith("enrollment_new") &&
            currentRoute.substringAfter("studentId=", "").substringBefore("&").isBlank()
    }
    return currentRoute == itemRoute || currentRoute.startsWith("$itemRoute?")
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
                route = Screen.EnrollmentNew.create(),
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
