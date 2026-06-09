package com.saicomputer.sms.feature.exports

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.SubpageTitleBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.dto.ExportResponse
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.core.ui.theme.appDimens


@Composable
fun ExportsScreen(
    user: User? = null,
    onBack: () -> Unit,
    snackbarController: SnackbarController,
    viewModel: ExportsViewModel = hiltViewModel()
) {
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun handle(res: ExportResponse, fallbackName: String) {
        val name = res.filename ?: fallbackName
        when {
            !res.downloadUrl.isNullOrBlank() -> enqueueDownload(context, res.downloadUrl, name)
            !res.viewUrl.isNullOrBlank() -> openUrl(context, res.viewUrl)
            else -> snackbarController.show(scope, "Nothing to export")
        }
        if (!res.downloadUrl.isNullOrBlank() || !res.viewUrl.isNullOrBlank()) {
            snackbarController.show(scope, "Exported ${res.rowCount} rows: $name")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ExportsListHeader(user = user, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = appDimens().spacingLg, vertical = appDimens().spacingMd),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacingMd)
        ) {
            ExportsInfoBanner()

            ExportCard(
                icon = Icons.Outlined.People,
                title = "Export Students",
                subtitle = "All student records with status and era",
                buttonLabel = "Export Students",
                loading = busy == ExportKind.Students,
                enabled = busy == null,
                onExport = {
                    viewModel.export(
                        ExportKind.Students,
                        { handle(it, "students.csv") },
                        { snackbarController.show(scope, it) }
                    )
                }
            )

            ExportCard(
                icon = Icons.Outlined.CreditCard,
                title = "Export Payments",
                subtitle = "Complete payment history with receipts",
                buttonLabel = "Export Payments",
                loading = busy == ExportKind.Payments,
                enabled = busy == null,
                onExport = {
                    viewModel.export(
                        ExportKind.Payments,
                        { handle(it, "payments.csv") },
                        { snackbarController.show(scope, it) }
                    )
                }
            )

            ExportCard(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                title = "Export Enrollments",
                subtitle = "All enrollments with installment details",
                buttonLabel = "Export Enrollments",
                loading = busy == ExportKind.Enrollments,
                enabled = busy == null,
                onExport = {
                    viewModel.export(
                        ExportKind.Enrollments,
                        { handle(it, "enrollments.csv") },
                        { snackbarController.show(scope, it) }
                    )
                }
            )
        }
    }
}

@Composable
private fun ExportsListHeader(user: User?, onBack: () -> Unit) {
    SubpageTitleBar(title = "Exports", onBack = onBack, user = user)
}

@Composable
private fun ExportsInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(appDimens().strokeHairline, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), appDimens().fieldShape)
            .padding(appDimens().spacingMd),
        horizontalArrangement = Arrangement.spacedBy(appDimens().spacing10),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeMd))
        Text(
            "Exports are generated as CSV files and ready for download instantly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExportCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    buttonLabel: String,
    loading: Boolean,
    enabled: Boolean,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = appDimens().cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = appDimens().strokeHairline)
    ) {
        Column(
            modifier = Modifier.padding(appDimens().spacingLg),
            verticalArrangement = Arrangement.spacedBy(appDimens().spacing14)
        ) {
            Row(
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
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeListInner))
                }
                Column(verticalArrangement = Arrangement.spacedBy(appDimens().spacingXxs)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ExportActionButton(
                label = buttonLabel,
                loading = loading,
                enabled = enabled,
                onClick = onExport
            )
        }
    }
}

@Composable
private fun ExportActionButton(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(appDimens().strokeHairline, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), appDimens().fieldShape)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(vertical = appDimens().spacingMd),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(appDimens().iconSizeSm),
                strokeWidth = appDimens().spacingXxs,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(Icons.Outlined.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(appDimens().iconSizeSm))
            Spacer(Modifier.size(appDimens().spacing6))
            Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun enqueueDownload(context: Context, url: String, name: String) {
    runCatching {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, name)
        (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
