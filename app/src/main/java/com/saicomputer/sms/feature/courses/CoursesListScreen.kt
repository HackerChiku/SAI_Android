package com.saicomputer.sms.feature.courses

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SubpageTitleBar
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@Composable
fun CoursesListScreen(
    user: User? = null,
    onBack: () -> Unit,
    onNewCourse: () -> Unit,
    onOpenCourse: (String) -> Unit,
    viewModel: CoursesListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val canCreate = can(user, "courses.create")

    Column(modifier = Modifier.fillMaxSize()) {
        CoursesListHeader(
            user = user,
            canCreate = canCreate,
            onBack = onBack,
            onNewCourse = onNewCourse
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = appDimens().spacingLg)
        ) {
            Spacer(Modifier.height(appDimens().spacingMd))

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
                            title = "No courses yet",
                            actionLabel = if (canCreate) "New Course" else null,
                            onAction = if (canCreate) onNewCourse else null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = appDimens().spacingLg),
                            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
                        ) {
                            items(s.data, key = { it.courseId }) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onOpenCourse(course.courseId) }
                                )
                            }
                            if (canCreate) {
                                item {
                                    Spacer(Modifier.height(appDimens().spacingXs))
                                    NewCourseButton(onClick = onNewCourse)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesListHeader(
    user: User?,
    canCreate: Boolean,
    onBack: () -> Unit,
    onNewCourse: () -> Unit
) {
    SubpageTitleBar(
        title = "Courses",
        onBack = onBack,
        user = user,
        actions = {
            if (canCreate) {
                Box(
                    modifier = Modifier
                        .size(appDimens().iconSizeXxl)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable(onClick = onNewCourse),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "New course",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
            }
        }
    )
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    val courseFeeLabel = if (course.billingType == BillingType.Subscription) {
        "${Formatters.formatInr(course.monthlyFee)}/mo"
    } else {
        Formatters.formatInr(course.fee)
    }

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
                Box(
                    modifier = Modifier
                        .size(appDimens().avatarSizeList)
                        .clip(appDimens().iconShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(appDimens().iconSizeListInner)
                    )
                }
                Text(
                    course.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                CourseStatusIndicator(isActive = course.isActive)
            }

            Text(
                course.courseFullName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
                Pill(
                    text = course.billingType.name,
                    color = appColors().info,
                    style = MaterialTheme.typography.labelMedium
                )
                if (course.generateCertificate) {
                    Pill(text = "Certificate", color = appColors().success, style = MaterialTheme.typography.labelMedium)
                }
                if (course.hasTopics) {
                    val count = course.topicsCount ?: 0
                    val label = if (count == 1) "1 Topic" else "$count Topics"
                    Pill(text = label, color = appColors().warning, style = MaterialTheme.typography.labelMedium)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth()) {
                CourseStatColumn(
                    label = "Course Fee",
                    value = courseFeeLabel,
                    modifier = Modifier.weight(1f)
                )
                CourseStatColumn(
                    label = "Enrollment Fee",
                    value = Formatters.formatInr(course.enrollmentFee),
                    modifier = Modifier.weight(1f)
                )
                CourseStatColumn(
                    label = "Max Installments",
                    value = course.maxInstallments.toString(),
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
private fun CourseStatusIndicator(isActive: Boolean) {
    val color = if (isActive) appColors().success else appColors().neutral
    val label = if (isActive) "Active" else "Inactive"

    Row(
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacing5),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(appDimens().iconDotSm)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CourseStatColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
    }
}

@Composable
private fun NewCourseButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(appDimens().strokeHairline, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), appDimens().fieldShape)
            .clickable(onClick = onClick)
            .padding(vertical = appDimens().spacing14),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(appDimens().iconSizeSm)
        )
        Spacer(Modifier.size(appDimens().spacing6))
        Text(
            "New Course",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
