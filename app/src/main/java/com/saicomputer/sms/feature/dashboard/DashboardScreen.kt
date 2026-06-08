package com.saicomputer.sms.feature.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.PhotoAvatar
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.BrandRedTint
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardPendingStudent
import com.saicomputer.sms.data.model.DashboardSummaryResponse
import com.saicomputer.sms.data.model.User

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
    val iconTint: androidx.compose.ui.graphics.Color,
    val iconBackground: androidx.compose.ui.graphics.Color,
    val highlighted: Boolean = false
)

@Composable
fun DashboardScreen(
    user: User?,
    onOpenStudent: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
                DashboardPhase.Loading -> LoadingSkeleton(Modifier.fillMaxSize())
                DashboardPhase.Error ->
                    ErrorState(
                        message = state.error ?: "Something went wrong",
                        onRetry = viewModel::retry,
                        modifier = Modifier.fillMaxSize()
                    )
                DashboardPhase.Content -> {
                    val summary = state.summary
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (state.refreshing) 0.5f else 1f)
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
    val initial = user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BaseWhite
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandBlue.copy(alpha = 0.6f))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(periodLabel, color = BaseWhite, style = MaterialTheme.typography.labelLarge)
                        Icon(
                            Icons.Outlined.ArrowDropDown,
                            contentDescription = "Change period",
                            tint = BaseWhite
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandBlue.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, color = BaseWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
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
    val cards = listOf(
        StatCard(
            "New Enrollments",
            summary.totalNewEnrollments.toString(),
            periodLabel,
            Icons.AutoMirrored.Outlined.MenuBook,
            BrandBlue,
            BrandBlueTint
        ),
        StatCard(
            "Fee Collected",
            Formatters.formatInr(summary.totalFeeCollected),
            periodLabel,
            Icons.Outlined.CurrencyRupee,
            BrandBlue,
            BrandBlueTint
        ),
        StatCard(
            "Fee Due",
            Formatters.formatInr(summary.totalFeeDueThisMonth),
            "Current month only",
            Icons.Outlined.Schedule,
            BrandBlue,
            BrandBlueTint
        ),
        StatCard(
            "Active Enrollments",
            summary.activeEnrollments.toString(),
            null,
            Icons.Outlined.People,
            BrandBlue,
            BrandBlueTint
        ),
        StatCard(
            "Completed",
            summary.completedThisPeriod.toString(),
            periodLabel,
            Icons.Outlined.CheckCircle,
            BrandBlue,
            BrandBlueTint
        ),
        StatCard(
            "Payment Pending",
            summary.paymentPendingThisMonth.toString(),
            null,
            Icons.Outlined.ErrorOutline,
            BrandRed,
            BrandRedTint,
            highlighted = true
        )
    )

    val breakdown = summary.paymentMethodBreakdown
    val methodTotal = breakdown.UPI + breakdown.CASH + breakdown.QR

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cards.chunked(2).forEach { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCards.forEach { card ->
                            StatCardView(
                                card = card,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(STAT_CARD_HEIGHT)
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PaymentMethodsChart(
                        breakdown = breakdown,
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        PaymentMethodLegendItem("UPI", breakdown.UPI, methodTotal, BrandBlue)
                        PaymentMethodLegendItem("Cash", breakdown.CASH, methodTotal, BrandRed)
                        PaymentMethodLegendItem("QR", breakdown.QR, methodTotal, androidx.compose.ui.graphics.Color(0xFF16A34A))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Payment Pending Students",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (pending.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BrandRedTint)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            pending.size.toString(),
                            color = BrandRed,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        if (pending.isEmpty()) {
            item {
                Text(
                    "No pending payments this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariantLightColor
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BaseWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        pending.forEachIndexed { index, student ->
                            if (index > 0) {
                                HorizontalDivider(color = OutlineVariantLight)
                            }
                            PendingStudentRow(student = student, onClick = { onOpenStudent(student.studentId) })
                        }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PhotoAvatar(name = student.fullName, size = 44)
        Column(modifier = Modifier.weight(1f)) {
            Text(student.fullName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                Formatters.formatPhone(student.phoneNumber),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariantLightColor
            )
        }
        Text(
            "${Formatters.formatInr(student.totalPendingThisMonth)} due",
            color = BrandRed,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private val STAT_CARD_HEIGHT = 128.dp

@Composable
private fun StatCardView(card: StatCard, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.highlighted) BrandRedTint.copy(alpha = 0.35f) else BaseWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(card.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    card.icon,
                    contentDescription = null,
                    tint = card.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                card.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (card.highlighted) BrandRed else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                card.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (card.subtitle != null) {
                Text(
                    card.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariantLightColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLightColor
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
