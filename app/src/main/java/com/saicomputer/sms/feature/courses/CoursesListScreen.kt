package com.saicomputer.sms.feature.courses

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
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandBlueTint
import com.saicomputer.sms.core.ui.theme.BrandRed
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.OutlineVariantLight
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.User

private val CardShape = RoundedCornerShape(14.dp)
private val IconShape = RoundedCornerShape(10.dp)
private val FieldShape = RoundedCornerShape(12.dp)

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
                            title = "No courses yet",
                            actionLabel = if (canCreate) "New Course" else null,
                            onAction = if (canCreate) onNewCourse else null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.data, key = { it.courseId }) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onOpenCourse(course.courseId) }
                                )
                            }
                            if (canCreate) {
                                item {
                                    Spacer(Modifier.height(4.dp))
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandRed)
                        .clickable(onClick = onNewCourse),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = "New course",
                        tint = BaseWhite,
                        modifier = Modifier.size(22.dp)
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(IconShape)
                        .background(BrandBlueTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(22.dp)
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
                color = OnSurfaceVariantLightColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(
                    text = course.billingType.name,
                    color = StatusBlue,
                    fontSize = 10.sp
                )
                if (course.generateCertificate) {
                    Pill(text = "Certificate", color = StatusEmerald, fontSize = 10.sp)
                }
                if (course.hasTopics) {
                    val count = course.topicsCount ?: 0
                    val label = if (count == 1) "1 Topic" else "$count Topics"
                    Pill(text = label, color = StatusAmber, fontSize = 10.sp)
                }
            }

            HorizontalDivider(color = OutlineVariantLight)

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
    val color = if (isActive) StatusEmerald else StatusGray
    val label = if (isActive) "Active" else "Inactive"

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
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
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariantLightColor,
            fontSize = 10.sp
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
            .clip(FieldShape)
            .background(BrandBlueTint)
            .border(1.dp, BrandBlue.copy(alpha = 0.35f), FieldShape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = null,
            tint = BrandBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(6.dp))
        Text(
            "New Course",
            color = BrandBlue,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
