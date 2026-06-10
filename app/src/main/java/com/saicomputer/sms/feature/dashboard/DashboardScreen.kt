package com.saicomputer.sms.feature.dashboard

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.DashboardLoadingSkeleton
import com.saicomputer.sms.core.ui.PhotoAvatar
import com.saicomputer.sms.core.ui.ProfileMenuButton
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardPendingStudent
import com.saicomputer.sms.data.model.DashboardSummaryResponse
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens

private val PERIOD_LABELS = mapOf(
    DashboardPeriod.thisMonth to "This Month",
    DashboardPeriod.lastMonth to "Last Month",
    DashboardPeriod.lastQuarter to "Last Quarter",
    DashboardPeriod.lastHalfYear to "Last 6 Months",
    DashboardPeriod.lastYear to "Last Year"
)

private data class StatCard(
    val title: String,
    val value: String,
    val subtitle: String?,
    val icon: ImageVector,
    val accent: StatCardAccent,
    val highlighted: Boolean = false
)

private enum class StatCardAccent {
    Blue, Green, Purple, Cyan, Red
}

@Composable
private fun isDashboardDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
private fun StatCardAccent.iconTint(): Color = if (isDashboardDarkTheme()) {
    when (this) {
        StatCardAccent.Blue -> appColors().accentBlue
        StatCardAccent.Green -> appColors().accentGreen
        StatCardAccent.Purple -> appColors().accentPurple
        StatCardAccent.Cyan -> appColors().accentCyan
        StatCardAccent.Red -> appColors().accentRed
    }
} else {
    when (this) {
        StatCardAccent.Blue -> MaterialTheme.colorScheme.primary
        StatCardAccent.Green -> appColors().success
        StatCardAccent.Purple -> appColors().purple
        StatCardAccent.Cyan -> MaterialTheme.colorScheme.primary
        StatCardAccent.Red -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun StatCardAccent.iconBackground(): Color = iconTint().copy(alpha = if (isDashboardDarkTheme()) 0.18f else 0.12f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User?,
    onOpenStudent: (String) -> Unit,
    snackbarController: SnackbarController? = null,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { message ->
            snackbarController?.show(scope, message)
        }
    }

    val phase = when {
        state.loading -> DashboardPhase.Loading
        state.error != null && state.summary == null -> DashboardPhase.Error
        else -> DashboardPhase.Content
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DashboardHeader(
            user = user,
            period = state.period,
            onPeriodChange = viewModel::setPeriod
        )

        Crossfade(
            targetState = phase,
            animationSpec = tween(220),
            modifier = Modifier.weight(1f),
            label = "dashboardPhase"
        ) { p ->
            when (p) {
                DashboardPhase.Loading -> DashboardLoadingSkeleton(Modifier.fillMaxSize())
                DashboardPhase.Error ->
                    ErrorState(
                        message = state.error ?: "Something went wrong",
                        onRetry = viewModel::retry,
                        modifier = Modifier.fillMaxSize()
                    )
                DashboardPhase.Content -> {
                    val summary = state.summary
                    PullToRefreshBox(
                        isRefreshing = state.refreshing,
                        onRefresh = viewModel::manualRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (summary != null) {
                            DashboardContent(
                                summary = summary,
                                period = state.period,
                                pending = state.pendingStudents,
                                onOpenStudent = onOpenStudent
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class DashboardPhase { Loading, Error, Content }

@Composable
private fun DashboardHeader(
    user: User?,
    period: DashboardPeriod,
    onPeriodChange: (DashboardPeriod) -> Unit
) {
    val periodLabel = PERIOD_LABELS[period] ?: "This Month"
    var expanded by remember { mutableStateOf(false) }
    val isDark = isDashboardDarkTheme()
    val headerBackground = if (isDark) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary
    val titleColor = if (isDark) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface
    val mutedColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)

    AppTopBarBox(containerColor = headerBackground) {
        AppTitleBarRow(
            leading = {
                Column {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    user?.fullName?.takeIf { it.isNotBlank() }?.let { name ->
                        Text(
                            "Welcome, $name 👋",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mutedColor,
                            modifier = Modifier.padding(top = appDimens().spacingXs)
                        )
                    }
                }
            },
            actions = {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(appDimens().logoShape)
                            .background(
                                if (isDark) appColors().surfaceElevated else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            .then(
                                if (isDark) {
                                    Modifier.border(appDimens().strokeHairline, appColors().outline, appDimens().logoShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { expanded = true }
                            .padding(horizontal = appDimens().spacingMd, vertical = appDimens().spacing6),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)
                    ) {
                        Text(
                            periodLabel,
                            color = titleColor,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Icon(
                            Icons.Outlined.ArrowDropDown,
                            contentDescription = "Change period",
                            tint = titleColor
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DashboardPeriod.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(PERIOD_LABELS[p] ?: p.name) },
                                onClick = {
                                    expanded = false
                                    onPeriodChange(p)
                                }
                            )
                        }
                    }
                }
                ProfileMenuButton(user = user)
            }
        )
    }
}

@Composable
private fun DashboardContent(
    summary: DashboardSummaryResponse,
    period: DashboardPeriod,
    pending: List<DashboardPendingStudent>,
    onOpenStudent: (String) -> Unit
) {
    val periodLabel = PERIOD_LABELS[period] ?: "This Month"
    val isDark = isDashboardDarkTheme()
    val cards = listOf(
        StatCard(
            "New Enrollments",
            summary.totalNewEnrollments.toString(),
            periodLabel,
            Icons.AutoMirrored.Outlined.MenuBook,
            StatCardAccent.Blue
        ),
        StatCard(
            "Fee Collected",
            Formatters.formatInr(summary.totalFeeCollected),
            periodLabel,
            Icons.Outlined.CurrencyRupee,
            StatCardAccent.Green
        ),
        StatCard(
            "Fee Due",
            Formatters.formatInr(summary.totalFeeDueThisMonth),
            "Current month only",
            Icons.Outlined.Schedule,
            StatCardAccent.Purple
        ),
        StatCard(
            "Active Enrollments",
            summary.activeEnrollments.toString(),
            null,
            Icons.Outlined.People,
            StatCardAccent.Cyan
        ),
        StatCard(
            "Completed",
            summary.completedThisPeriod.toString(),
            periodLabel,
            Icons.Outlined.CheckCircle,
            StatCardAccent.Green
        ),
        StatCard(
            "Payment Pending",
            summary.paymentPendingThisMonth.toString(),
            if (isDark) "Needs Attention" else null,
            Icons.Outlined.ErrorOutline,
            StatCardAccent.Red,
            highlighted = true
        )
    )

    val breakdown = summary.paymentMethodBreakdown
    val methodTotal = breakdown.UPI + breakdown.CASH + breakdown.QR

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(appDimens().spacingLg),
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)) {
                cards.chunked(2).forEach { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
                    ) {
                        rowCards.forEach { card ->
                            StatCardView(
                                card = card,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(appDimens().statCardHeight)
                            )
                        }
                        if (rowCards.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            DashboardSectionCard(
                title = "Monthly Revenue",
                subtitle = "Last 6 months"
            ) {
                MonthlyRevenueChart(points = summary.monthlyRevenueTrend)
            }
        }

        item {
            DashboardSectionCard(
                title = "Payment Methods",
                subtitle = periodLabel
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacingLg)
                ) {
                    PaymentMethodsChart(
                        breakdown = breakdown,
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(appDimens().spacing10),
                        modifier = Modifier.weight(1f)
                    ) {
                        val isDark = isDashboardDarkTheme()
                        PaymentMethodLegendItem(
                            "UPI",
                            breakdown.UPI,
                            methodTotal,
                            if (isDark) appColors().accentBlue else MaterialTheme.colorScheme.primary
                        )
                        PaymentMethodLegendItem(
                            "Cash",
                            breakdown.CASH,
                            methodTotal,
                            if (isDark) appColors().accentRed else MaterialTheme.colorScheme.tertiary
                        )
                        PaymentMethodLegendItem(
                            "QR",
                            breakdown.QR,
                            methodTotal,
                            if (isDark) appColors().accentGreen else appColors().success
                        )
                    }
                }
            }
        }

        item {
            PaymentPendingStudentsSection(
                pending = pending,
                onOpenStudent = onOpenStudent
            )
        }
    }
}

@Composable
private fun PaymentPendingStudentsSection(
    pending: List<DashboardPendingStudent>,
    onOpenStudent: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val studentLabel = if (pending.size == 1) "1 student" else "${pending.size} students"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDashboardDarkTheme()) {
                    Modifier.border(appDimens().strokeHairline, appColors().outline, appDimens().statShape)
                } else {
                    Modifier
                }
            ),
        shape = appDimens().statShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().spacingNone)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (pending.isNotEmpty()) Modifier.clickable { expanded = !expanded }
                        else Modifier
                    )
                    .padding(appDimens().spacingLg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Payment Pending Students",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (pending.isEmpty()) {
                            "No pending payments this month."
                        } else {
                            studentLabel
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (pending.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isDashboardDarkTheme()) appColors().accentRed.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.tertiaryContainer
                                )
                                .padding(horizontal = appDimens().spacing10, vertical = appDimens().spacingXs)
                        ) {
                            Text(
                                pending.size.toString(),
                                color = if (isDashboardDarkTheme()) appColors().accentRed else MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse payment pending students" else "Expand payment pending students",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded && pending.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    pending.forEachIndexed { index, student ->
                        if (index > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        PendingStudentRow(
                            student = student,
                            onClick = { onOpenStudent(student.studentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodLegendItem(
    label: String,
    value: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
    ) {
        Box(
            modifier = Modifier
                .size(appDimens().spacing10)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            "$label  ${paymentMethodPercent(value, total)}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PendingStudentRow(
    student: DashboardPendingStudent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacing14),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
    ) {
        PhotoAvatar(name = student.fullName, size = 44)
        Column(modifier = Modifier.weight(1f)) {
            Text(student.fullName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                Formatters.formatPhone(student.phoneNumber),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${Formatters.formatInr(student.totalPendingThisMonth)} due",
            color = if (isDashboardDarkTheme()) appColors().accentRed else MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun StatCardView(card: StatCard, modifier: Modifier = Modifier) {
    val isDark = isDashboardDarkTheme()
    val iconTint = card.accent.iconTint()
    val iconBackground = card.accent.iconBackground()
    val containerColor = when {
        isDark && card.highlighted -> MaterialTheme.colorScheme.surface
        isDark -> MaterialTheme.colorScheme.surface
        card.highlighted -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isDark && card.highlighted -> appColors().accentRed.copy(alpha = 0.55f)
        isDark -> appColors().outline
        else -> Color.Transparent
    }
    val valueColor = when {
        card.highlighted && !isDark -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subtitleColor = when {
        card.highlighted && isDark -> appColors().accentRed.copy(alpha = 0.75f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(appDimens().strokeHairline, borderColor, appDimens().statShape)
                } else {
                    Modifier
                }
            ),
        shape = appDimens().statShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().spacingNone)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(appDimens().spacing14)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(appDimens().iconSizeXxl)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    card.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(appDimens().iconSizeMd)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(end = appDimens().callButtonSize),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
            ) {
                Text(
                    card.value,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    softWrap = true,
                    maxLines = 2
                )
                Text(
                    card.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    softWrap = true,
                    maxLines = 2
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = appDimens().spacing28)
                ) {
                    if (card.subtitle != null) {
                        Text(
                            card.subtitle,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor,
                            softWrap = true,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDashboardDarkTheme()) {
                    Modifier.border(appDimens().strokeHairline, appColors().outline, appDimens().statShape)
                } else {
                    Modifier
                }
            ),
        shape = appDimens().statShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().spacingNone)
    ) {
        Column(modifier = Modifier.padding(appDimens().spacingLg)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(appDimens().spacingMd))
            content()
        }
    }
}
