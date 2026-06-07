package com.saicomputer.sms.feature.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.PhotoAvatar
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardSummaryResponse

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
    val icon: ImageVector
)

@Composable
fun DashboardScreen(
    onOpenStudent: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val phase = when {
        state.loading -> DashboardPhase.Loading
        state.error != null && state.summary == null -> DashboardPhase.Error
        else -> DashboardPhase.Content
    }

    Crossfade(targetState = phase, animationSpec = tween(220), label = "dashboardPhase") { p ->
        when (p) {
            DashboardPhase.Loading -> LoadingSkeleton()
            DashboardPhase.Error ->
                ErrorState(message = state.error ?: "Something went wrong", onRetry = viewModel::retry)
            DashboardPhase.Content -> {
                val summary = state.summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.refreshing) 0.5f else 1f)
                ) {
                    if (summary != null) {
                        DashboardContent(
                            summary = summary,
                            period = state.period,
                            onPeriodChange = viewModel::setPeriod,
                            pending = state.pendingStudents,
                            onOpenStudent = onOpenStudent
                        )
                    }
                }
            }
        }
    }
}

private enum class DashboardPhase { Loading, Error, Content }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    summary: DashboardSummaryResponse,
    period: DashboardPeriod,
    onPeriodChange: (DashboardPeriod) -> Unit,
    pending: List<com.saicomputer.sms.data.model.DashboardPendingStudent>,
    onOpenStudent: (String) -> Unit
) {
    val periodLabel = PERIOD_LABELS[period] ?: "This Month"
    val cards = listOf(
        StatCard("New Enrollments", summary.totalNewEnrollments.toString(), periodLabel, Icons.AutoMirrored.Outlined.MenuBook),
        StatCard("Fee Collected", Formatters.formatInr(summary.totalFeeCollected), periodLabel, Icons.Outlined.CurrencyRupee),
        StatCard("Fee Due This Month", Formatters.formatInr(summary.totalFeeDueThisMonth), "Current month only", Icons.Outlined.Schedule),
        StatCard("Active Enrollments", summary.activeEnrollments.toString(), null, Icons.Outlined.People),
        StatCard("Completed", summary.completedThisPeriod.toString(), periodLabel, Icons.Outlined.CheckCircle),
        StatCard("Payment Pending", summary.paymentPendingThisMonth.toString(), null, Icons.Outlined.ErrorOutline)
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dashboard", style = MaterialTheme.typography.titleLarge)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(periodLabel)
                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
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
            }
        }

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
            SectionCard(title = "Monthly Revenue") {
                MonthlyRevenueChart(points = summary.monthlyRevenueTrend)
            }
        }

        item {
            SectionCard(title = "Payment Methods") {
                PaymentMethodsChart(breakdown = summary.paymentMethodBreakdown)
            }
        }

        item {
            Text("Payment Pending Students", style = MaterialTheme.typography.titleMedium)
        }

        if (pending.isEmpty()) {
            item {
                Text(
                    "No pending payments this month.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(pending) { student ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStudent(student.studentId) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PhotoAvatar(name = student.fullName, size = 40)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, fontWeight = FontWeight.SemiBold)
                            Text(
                                Formatters.formatPhone(student.phoneNumber),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${Formatters.formatInr(student.totalPendingThisMonth)} pending",
                            color = StatusRed,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private val STAT_CARD_HEIGHT = 132.dp

@Composable
private fun StatCardView(card: StatCard, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(card.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                card.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                card.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (card.subtitle != null) {
                Text(
                    card.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
