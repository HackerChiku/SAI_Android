package com.saicomputer.sms.feature.enrollments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.ProfileMenuButton
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus
import com.saicomputer.sms.data.model.User

private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun EnrollmentsListScreen(
    user: User? = null,
    onNewEnrollment: () -> Unit,
    onOpenEnrollment: (String) -> Unit,
    viewModel: EnrollmentsListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        EnrollmentsListHeader(user = user, onNewEnrollment = onNewEnrollment)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            when (val s = state) {
                is UiState.Loading -> LoadingSkeleton(modifier = Modifier.fillMaxSize())
                is UiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = viewModel::load,
                    modifier = Modifier.fillMaxSize()
                )
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState(
                            title = "No enrollments yet",
                            actionLabel = "New Enrollment",
                            onAction = onNewEnrollment,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.data, key = { it.enrollment.enrollmentId }) { item ->
                                EnrollmentCard(
                                    item = item,
                                    onClick = { onOpenEnrollment(item.enrollment.enrollmentId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnrollmentsListHeader(user: User?, onNewEnrollment: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Enrollments",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BrandRed)
                    .clickable(onClick = onNewEnrollment),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "New enrollment", tint = BaseWhite, modifier = Modifier.size(22.dp))
            }
            ProfileMenuButton(user = user)
        }
    }
}

@Composable
private fun EnrollmentCard(item: EnrollmentListItem, onClick: () -> Unit) {
    val enrollment = item.enrollment
    val studentName = item.studentName
    val courseName = item.courseName
    val (statusLabel, statusColor) = enrollmentStatusDisplay(enrollment)
    val billingLabel = when (enrollment.billingType) {
        BillingType.Subscription -> "Subscription"
        BillingType.Installment, null -> "Installment"
    }
    val paidPercent = enrollmentPaidPercent(enrollment)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredPhotoAvatar(name = studentName, size = 44)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        courseName,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariantLightColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(text = statusLabel, color = statusColor, fontSize = 10.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill(text = billingLabel, color = StatusBlue, fontSize = 10.sp)
                Text(
                    Formatters.formatDateIst(enrollment.startDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariantLightColor
                )
            }

            if (enrollment.billingType == BillingType.Subscription) {
                enrollment.paidThroughDate?.takeIf { it.isNotBlank() }?.let { paidThrough ->
                    Text(
                        "Paid through: ${Formatters.formatDateIst(paidThrough)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariantLightColor
                    )
                }
            } else if (enrollment.totalAmountDue > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariantLightColor)
                        Text(
                            "$paidPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { paidPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BrandBlue,
                        trackColor = OffWhite
                    )
                }
            }
        }
    }
}

private fun enrollmentStatusDisplay(enrollment: Enrollment): Pair<String, androidx.compose.ui.graphics.Color> {
    return when {
        enrollment.enrollmentStatus == EnrollmentStatus.Completed ->
            "Completed" to StatusGray
        enrollment.enrollmentStatus == EnrollmentStatus.Cancelled ->
            "Cancelled" to StatusAmber
        enrollment.balance > 0 ->
            "Pmt Pending" to StatusAmber
        else ->
            "Active" to StatusEmerald
    }
}

private fun enrollmentPaidPercent(enrollment: Enrollment): Int {
    if (enrollment.totalAmountDue <= 0) return 100
    return ((enrollment.totalAmountPaid.toLong() * 100) / enrollment.totalAmountDue).toInt().coerceIn(0, 100)
}
