package com.saicomputer.sms.feature.enrollments

import com.saicomputer.sms.core.ui.theme.appColors
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.ColoredPhotoAvatar
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.FilterDialogShell
import com.saicomputer.sms.core.ui.FilterDropdown
import com.saicomputer.sms.core.ui.ListSearchFilterSortBar
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.ProfileMenuButton
import com.saicomputer.sms.core.ui.SortDialog
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@Composable
fun EnrollmentsListScreen(
    user: User? = null,
    onNewEnrollment: () -> Unit,
    onOpenEnrollment: (String) -> Unit,
    viewModel: EnrollmentsListViewModel = hiltViewModel()
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val displayItems by viewModel.displayItems.collectAsStateWithLifecycle()
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    val courseOptions = remember(courses) {
        listOf("All") + courses.map { it.courseId }
    }
    val courseLabels = remember(courses) {
        mapOf("All" to "All Courses") + courses.associate { it.courseId to it.label }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EnrollmentsListHeader(user = user, onNewEnrollment = onNewEnrollment)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = appDimens().spacingLg)
        ) {
            Spacer(Modifier.height(appDimens().spacingMd))

            ListSearchFilterSortBar(
                search = filters.search,
                onSearchChange = viewModel::onSearchChange,
                searchPlaceholder = "Search by student or course",
                hasActiveFilters = filters.hasActiveFilters,
                hasActiveSort = filters.hasActiveSort,
                onFilterClick = { showFilterDialog = true },
                onSortClick = { showSortDialog = true }
            )

            if (showFilterDialog) {
                EnrollmentsFilterDialog(
                    displayStatus = filters.displayStatus,
                    billingType = filters.billingType,
                    courseId = filters.courseId,
                    courseOptions = courseOptions,
                    courseLabels = courseLabels,
                    onDisplayStatusChange = viewModel::onDisplayStatusChange,
                    onBillingTypeChange = viewModel::onBillingTypeChange,
                    onCourseChange = viewModel::onCourseChange,
                    onClear = viewModel::clearFilterFields,
                    onDismiss = { showFilterDialog = false }
                )
            }

            if (showSortDialog) {
                SortDialog(
                    title = "Sort",
                    options = EnrollmentSort.OPTIONS,
                    optionLabels = EnrollmentSort.LABELS,
                    selected = filters.sort,
                    onSelected = viewModel::onSortChange,
                    onDismiss = { showSortDialog = false }
                )
            }

            Spacer(Modifier.height(appDimens().spacingMd))

            when (val s = displayItems) {
                is UiState.Loading -> LoadingSkeleton(modifier = Modifier.fillMaxSize())
                is UiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = viewModel::load,
                    modifier = Modifier.fillMaxSize()
                )
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        if (viewModel.hasActiveClientFilters) {
                            EmptyState(
                                title = "No enrollments match your filters",
                                actionLabel = "Clear filters",
                                onAction = viewModel::clearFilters,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyState(
                                title = "No enrollments yet",
                                actionLabel = "New Enrollment",
                                onAction = onNewEnrollment,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = appDimens().spacingLg),
                            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnrollmentsFilterDialog(
    displayStatus: String,
    billingType: String,
    courseId: String,
    courseOptions: List<String>,
    courseLabels: Map<String, String>,
    onDisplayStatusChange: (String) -> Unit,
    onBillingTypeChange: (String) -> Unit,
    onCourseChange: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    FilterDialogShell(
        title = "Filters",
        onClear = onClear,
        onDismiss = onDismiss
    ) {
        FilterDropdown(
            displayLabel = EnrollmentDisplayStatus.LABELS[displayStatus] ?: "All Status",
            options = EnrollmentDisplayStatus.OPTIONS,
            optionLabels = EnrollmentDisplayStatus.LABELS,
            selected = displayStatus,
            onSelected = onDisplayStatusChange,
            modifier = Modifier.fillMaxWidth()
        )
        FilterDropdown(
            displayLabel = EnrollmentBillingFilter.LABELS[billingType] ?: "All Billing",
            options = EnrollmentBillingFilter.OPTIONS,
            optionLabels = EnrollmentBillingFilter.LABELS,
            selected = billingType,
            onSelected = onBillingTypeChange,
            modifier = Modifier.fillMaxWidth()
        )
        FilterDropdown(
            displayLabel = courseLabels[courseId] ?: "All Courses",
            options = courseOptions,
            optionLabels = courseLabels,
            selected = courseId,
            onSelected = onCourseChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EnrollmentsListHeader(user: User?, onNewEnrollment: () -> Unit) {
    AppTopBarBox {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = appDimens().iconSizeMd, vertical = appDimens().spacingLg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Enrollments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.surface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(appDimens().iconSizeXxl)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable(onClick = onNewEnrollment),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "New enrollment", tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(appDimens().iconSizeListInner))
                }
                ProfileMenuButton(user = user)
            }
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
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColoredPhotoAvatar(name = studentName, size = 44)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Pill(text = statusLabel, color = statusColor, style = MaterialTheme.typography.labelMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm), verticalAlignment = Alignment.CenterVertically) {
                Pill(text = billingLabel, color = appColors().info, style = MaterialTheme.typography.labelMedium)
                Text(
                    Formatters.formatDateIst(enrollment.startDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (enrollment.billingType == BillingType.Subscription) {
                enrollment.paidThroughDate?.takeIf { it.isNotBlank() }?.let { paidThrough ->
                    Text(
                        "Paid through: ${Formatters.formatDateIst(paidThrough)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (enrollment.totalAmountDue > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            .height(appDimens().spacing6)
                            .clip(RoundedCornerShape(appDimens().cornerRadiusProgress)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}

@Composable
private fun enrollmentStatusDisplay(enrollment: Enrollment): Pair<String, androidx.compose.ui.graphics.Color> {
    return when (enrollmentDisplayStatusKey(enrollment)) {
        EnrollmentDisplayStatus.COMPLETED ->
            "Completed" to appColors().neutral
        EnrollmentDisplayStatus.CANCELLED ->
            "Cancelled" to appColors().warning
        EnrollmentDisplayStatus.PAYMENT_PENDING ->
            "Pmt Pending" to appColors().warning
        else ->
            "Active" to appColors().success
    }
}

private fun enrollmentPaidPercent(enrollment: Enrollment): Int {
    if (enrollment.totalAmountDue <= 0) return 100
    return ((enrollment.totalAmountPaid.toLong() * 100) / enrollment.totalAmountDue).toInt().coerceIn(0, 100)
}
