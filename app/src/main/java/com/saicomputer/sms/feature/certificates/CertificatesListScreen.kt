package com.saicomputer.sms.feature.certificates

import com.saicomputer.sms.core.ui.theme.appColors
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.ui.EmptyState
import com.saicomputer.sms.core.ui.ErrorState
import com.saicomputer.sms.core.ui.LoadingSkeleton
import com.saicomputer.sms.core.ui.Pill
import com.saicomputer.sms.core.ui.SubpageTitleBar
import com.saicomputer.sms.core.ui.ResendEmailDialog
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.emailStatusColor
import com.saicomputer.sms.data.model.CertificateListItem
import com.saicomputer.sms.data.model.EmailStatus
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesListScreen(
    user: User? = null,
    onBack: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: CertificatesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var resendFor by remember { mutableStateOf<CertificateListItem?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.refreshError.collect { message ->
            snackbarController.show(scope, message)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CertificatesListHeader(user = user, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = appDimens().spacingLg)
        ) {
            Spacer(Modifier.height(appDimens().spacingMd))

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::manualRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val s = state) {
                    is UiState.Loading -> LoadingSkeleton(modifier = Modifier.fillMaxSize())
                    is UiState.Error -> ErrorState(
                        message = s.message,
                        onRetry = { viewModel.load(force = true) },
                        modifier = Modifier.fillMaxSize()
                    )
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            EmptyState(title = "No certificates", modifier = Modifier.fillMaxSize())
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = appDimens().spacingLg),
                                verticalArrangement = Arrangement.spacedBy(appDimens().spacing10)
                            ) {
                                items(s.data, key = { it.certificateId }) { certificate ->
                                    CertificateCard(
                                        certificate = certificate,
                                        onView = {
                                            viewModel.loadCertificate(
                                                certificate.certificateId,
                                                onSuccess = { detail ->
                                                    val url = detail.previewUrl ?: detail.downloadUrl
                                                    if (url != null) {
                                                        context.startActivity(
                                                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                        )
                                                    } else {
                                                        snackbarController.show(
                                                            scope,
                                                            "Certificate ${detail.certificateId}"
                                                        )
                                                    }
                                                },
                                                onError = { snackbarController.show(scope, it) }
                                            )
                                        },
                                        onResend = { resendFor = certificate }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    resendFor?.let { c ->
        ResendEmailDialog(
            defaultEmail = "",
            onConfirm = { email ->
                viewModel.resend(c.certificateId, email) { snackbarController.show(scope, it) }
                resendFor = null
            },
            onDismiss = { resendFor = null }
        )
    }
}

@Composable
private fun CertificatesListHeader(user: User?, onBack: () -> Unit) {
    SubpageTitleBar(title = "Certificates", onBack = onBack, user = user)
}

@Composable
private fun CertificateCard(
    certificate: CertificateListItem,
    onView: () -> Unit,
    onResend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    certificate.certificateId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Pill(
                    text = emailStatusLabel(certificate.emailStatus),
                    color = emailStatusPillColor(certificate.emailStatus),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                certificate.studentName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                certificate.courseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Issued: ${Formatters.formatDateIst(certificate.issueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.End) {
                    ListActionButton(
                        label = "View",
                        icon = Icons.Outlined.Visibility,
                        onClick = onView
                    )
                    ListActionButton(
                        label = "Resend",
                        icon = Icons.Outlined.Email,
                        onClick = onResend
                    )
                }
            }
        }
    }
}

@Composable
private fun ListActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeSm))
        Spacer(Modifier.size(appDimens().spacingXs))
        Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}

private fun emailStatusLabel(status: EmailStatus): String = when (status) {
    EmailStatus.NotSent -> "Not Sent"
    EmailStatus.NotApplicable -> "N/A"
    else -> status.name
}

@Composable
private fun emailStatusPillColor(status: EmailStatus) = when (status) {
    EmailStatus.NotSent, EmailStatus.NotApplicable -> appColors().neutral
    else -> emailStatusColor(status)
}
