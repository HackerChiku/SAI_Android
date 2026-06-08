package com.saicomputer.sms.feature.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Topic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.permission.can
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.CurrencyText
import com.saicomputer.sms.core.ui.CrossfadeUiState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.ui.ListItemCard
import com.saicomputer.sms.core.ui.ListItemIconBox
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.BrandGold
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.CourseTopic
import com.saicomputer.sms.data.model.PackageType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val canEdit = can(user, "courses.update")

    val topBarTitle = when (val s = state) {
        is UiState.Success -> s.data.course.courseName
        else -> "Course"
    }

    androidx.compose.runtime.LaunchedEffect(courseId) { viewModel.load(courseId) }

    Scaffold(
        topBar = { SmsTopBar(title = topBarTitle, onBack = onBack) },
        floatingActionButton = {
            if (canEdit && state is UiState.Success) {
                FloatingActionButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit course")
                }
            }
        }
    ) { padding ->
        CrossfadeUiState(
            state = state,
            modifier = Modifier.fillMaxSize().padding(padding),
            loading = { LoadingSkeleton(modifier = Modifier.fillMaxSize()) },
            error = { message ->
                ErrorState(message = message, onRetry = viewModel::reload, modifier = Modifier.fillMaxSize())
            },
            success = { data ->
                CourseDetailContent(course = data.course, topics = data.topics)
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
        BillingType.Installment -> BrandBlue
        BillingType.Subscription -> BrandGold
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ListItemCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    ListItemIconBox(icon = Icons.AutoMirrored.Outlined.MenuBook)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GenericBadge(course.billingType.name, billingColor)
                            if (course.generateCertificate) GenericBadge("Certificate", BrandBlue)
                            if (!course.isActive) {
                                GenericBadge("Inactive", MaterialTheme.colorScheme.error)
                            } else {
                                GenericBadge("Active", StatusEmerald)
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
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.width(14.dp).height(14.dp),
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
                    GenericBadge("${course.currentActiveEnrollments} active enrollments", StatusEmerald)
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
                                modifier = Modifier.padding(vertical = 8.dp),
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(18.dp).height(18.dp)
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
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
