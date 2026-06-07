package com.saicomputer.sms.feature.exports

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.ui.SmsTopBar
import com.saicomputer.sms.core.ui.SnackbarController
import com.saicomputer.sms.data.dto.ExportResponse

@Composable
fun ExportsScreen(
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

    Scaffold(topBar = { SmsTopBar(title = "Exports", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Export data as CSV", style = MaterialTheme.typography.titleMedium)
            ExportButton("Export Students", busy == ExportKind.Students) {
                viewModel.export(ExportKind.Students, { handle(it, "students.csv") }, { snackbarController.show(scope, it) })
            }
            ExportButton("Export Payments", busy == ExportKind.Payments) {
                viewModel.export(ExportKind.Payments, { handle(it, "payments.csv") }, { snackbarController.show(scope, it) })
            }
            ExportButton("Export Enrollments", busy == ExportKind.Enrollments) {
                viewModel.export(ExportKind.Enrollments, { handle(it, "enrollments.csv") }, { snackbarController.show(scope, it) })
            }
        }
    }
}

@Composable
private fun ExportButton(label: String, loading: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
        if (loading) CircularProgressIndicator(Modifier.padding(2.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        else Text(label)
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
