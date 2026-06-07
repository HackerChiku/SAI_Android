package com.saicomputer.sms.core.ui

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Full-screen PDF/document preview backed by a WebView pointed at the backend's
 * Google Docs viewer URL. "Download" enqueues [downloadUrl] via DownloadManager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewScreen(
    title: String,
    previewUrl: String?,
    downloadUrl: String?,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    actions()
                    if (downloadUrl != null) {
                        IconButton(onClick = { enqueueDownload(context, downloadUrl, title) }) {
                            Icon(Icons.Outlined.Download, contentDescription = "Download")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (previewUrl.isNullOrBlank()) {
                EmptyState(
                    title = "Preview not available",
                    description = "The PDF may still be generating. Try again shortly.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                var loading by remember { mutableStateOf(true) }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    loading = false
                                }
                            }
                            loadUrl(previewUrl)
                        }
                    }
                )
                if (loading) CircularProgressIndicator()
            }
        }
    }
}

private fun enqueueDownload(context: Context, url: String, title: String) {
    runCatching {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                android.os.Environment.DIRECTORY_DOWNLOADS,
                "$title.pdf"
            )
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}
