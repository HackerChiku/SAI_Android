package com.saicomputer.sms.feature.certificates

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.saicomputer.sms.core.ui.ResendEmailDialog
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.core.ui.emailStatusColor
import com.saicomputer.sms.core.ui.theme.BaseWhite
import com.saicomputer.sms.core.ui.theme.BrandBlue
import com.saicomputer.sms.core.ui.theme.OffWhite
import com.saicomputer.sms.core.ui.theme.OnSurfaceVariantLightColor
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.data.model.CertificateListItem
import com.saicomputer.sms.data.model.EmailStatus
import com.saicomputer.sms.data.model.User

private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun CertificatesListScreen(
    user: User? = null,
    snackbarController: SnackbarController,
    viewModel: CertificatesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var resendFor by remember { mutableStateOf<CertificateListItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        CertificatesListHeader(user = user)

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
                        EmptyState(title = "No certificates", modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
private fun CertificatesListHeader(user: User?) {
    val initial = user?.fullName?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Certificates",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BaseWhite
        )
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

@Composable
private fun CertificateCard(
    certificate: CertificateListItem,
    onView: () -> Unit,
    onResend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = BaseWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    certificate.certificateId,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariantLightColor,
                    fontWeight = FontWeight.Medium
                )
                Pill(
                    text = emailStatusLabel(certificate.emailStatus),
                    color = emailStatusPillColor(certificate.emailStatus),
                    fontSize = 10.sp
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
                    color = OnSurfaceVariantLightColor,
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
        Icon(icon, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(4.dp))
        Text(label, color = BrandBlue, fontWeight = FontWeight.SemiBold)
    }
}

private fun emailStatusLabel(status: EmailStatus): String = when (status) {
    EmailStatus.NotSent -> "Not Sent"
    EmailStatus.NotApplicable -> "N/A"
    else -> status.name
}

private fun emailStatusPillColor(status: EmailStatus) = when (status) {
    EmailStatus.NotSent, EmailStatus.NotApplicable -> StatusGray
    else -> emailStatusColor(status)
}
