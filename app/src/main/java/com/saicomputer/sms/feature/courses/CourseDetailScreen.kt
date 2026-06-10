package com.saicomputer.sms.feature.courses

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Topic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.AppTitleBarRow
import com.saicomputer.sms.core.ui.AppTopBarBox
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.CrossfadeUiState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.ListItemCard
import com.saicomputer.sms.core.ui.ListItemIconBox
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.TitleBarBackButton
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.CourseTopic
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.core.ui.theme.appDimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    snackbarController: SnackbarController? = null,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val canEdit = can(user, "courses.update")
    val showEdit = canEdit && state is UiState.Success

    LaunchedEffect(courseId) { viewModel.load(courseId) }
    LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { message ->
            snackbarController?.show(scope, message)
        }
    }

    CrossfadeUiState(
        state = state,
        loading = {
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                CourseDetailHeader(title = "Course", onBack = onBack, onEdit = null)
                LoadingSkeleton(modifier = Modifier.fillMaxSize())
            }
        },
        error = { message ->
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                CourseDetailHeader(title = "Course", onBack = onBack, onEdit = null)
                ErrorState(message = message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize())
            }
        },
        success = { data ->
            Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                CourseDetailHeader(
                    title = data.course.courseName,
                    onBack = onBack,
                    onEdit = if (showEdit) onEdit else null
                )
                PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = viewModel::manualRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CourseDetailContent(course = data.course, topics = data.topics)
                }
            }
        }
    )
}

@Composable
private fun CourseDetailHeader(
    title: String,
    onBack: () -> Unit,
    onEdit: (() -> Unit)?
) {
    AppTopBarBox {
        AppTitleBarRow(
            leading = {
                TitleBarBackButton(onBack = onBack)
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            actions = {
                if (onEdit != null) {
                    Box(
                        modifier = Modifier
                            .size(appDimens().iconSizeXxl)
                            .clip(CircleShape)
                            .clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit course",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(appDimens().iconSizeListInner)
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseDetailContent(
    course: Course,
    topics: List<CourseTopic>
) {
    val billingColor = when (course.billingType) {
        BillingType.Installment -> MaterialTheme.colorScheme.primary
        BillingType.Subscription -> appColors().brandSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(appDimens().spacingLg),
        verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
    ) {
        ListItemCard {
            Column(
                modifier = Modifier.padding(appDimens().spacingLg),
                verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(appDimens().spacing14),
                    verticalAlignment = Alignment.Top
                ) {
                    ListItemIconBox(icon = Icons.AutoMirrored.Outlined.MenuBook)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
                    ) {
                        Text(
                            course.courseName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            course.courseFullName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            course.courseId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(appDimens().spacing6)) {
                            GenericBadge(course.billingType.name, billingColor)
                            if (course.generateCertificate) GenericBadge("Certificate", MaterialTheme.colorScheme.primary)
                            if (!course.isActive) {
                                GenericBadge("Inactive", MaterialTheme.colorScheme.error)
                            } else {
                                GenericBadge("Active", appColors().success)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Course fee", amount = course.fee, emphasized = true)
                    StatItem(label = "Enrollment", amount = course.enrollmentFee)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(appDimens().spacingXs)
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.width(appDimens().spacing14).height(appDimens().spacing14),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${course.durationMonths} mo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            "Duration",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (course.currentActiveEnrollments > 0) {
                    GenericBadge("${course.currentActiveEnrollments} active enrollments", appColors().success)
                }
            }
        }

        DetailSection("Fees & billing", Icons.Outlined.Payments) {
            DetailInfoRow("Course fee", Formatters.formatInr(course.fee))
            DetailInfoRow("Enrollment fee", Formatters.formatInr(course.enrollmentFee))
            DetailInfoRow("Billing type", course.billingType.name)
            if (course.billingType == BillingType.Subscription) {
                DetailInfoRow("Monthly fee", Formatters.formatInr(course.monthlyFee))
                if (course.packageType != PackageType.NONE) {
                    DetailInfoRow("Package", packageLabel(course.packageType))
                    course.packagePaidMonths?.let { DetailInfoRow("Paid months", it.toString()) }
                    course.packageBonusMonths?.let { DetailInfoRow("Bonus months", it.toString()) }
                }
            } else {
                DetailInfoRow("Max installments", course.maxInstallments.toString())
            }
        }

        DetailSection("Course details", Icons.Outlined.Info) {
            course.category?.let { DetailInfoRow("Category", it) }
            course.description?.takeIf { it.isNotBlank() }?.let { DetailInfoRow("Description", it) }
            course.courseLink?.takeIf { it.isNotBlank() }?.let { DetailInfoRow("Course link", it) }
            DetailInfoRow("Certificate", if (course.generateCertificate) "Yes" else "No")
            DetailInfoRow("Topics tracking", if (course.hasTopics) "Enabled" else "Disabled")
        }

        if (course.hasTopics) {
            DetailSection("Topics (${topics.size})", Icons.Outlined.Topic) {
                if (topics.isEmpty()) {
                    Text(
                        "No topics configured yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    topics.forEachIndexed { index, topic ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = appDimens().spacingSm),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        TopicRow(topic)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, amount: Int, emphasized: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CurrencyText(
            amount,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            bold = true
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TopicRow(topic: CourseTopic) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingXs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                topic.topicName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!topic.isActive) {
                GenericBadge("Inactive", MaterialTheme.colorScheme.error)
            }
        }
        topic.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${topic.estimatedDurationValue} ${topic.estimatedDurationUnit.name.lowercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ListItemCard {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(appDimens().iconSizeSm).height(appDimens().iconSizeSm)
                )
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun packageLabel(type: PackageType): String = when (type) {
    PackageType.NONE -> "None"
    PackageType.PACKAGE_3_1 -> "3+1"
}
