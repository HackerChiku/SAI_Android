package com.saicomputer.sms.feature.students

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.ThemedShimmerBox
import com.saicomputer.sms.core.ui.rememberBase64ImageBitmap
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun PhotoViewerDialog(
    studentId: String,
    canReplace: Boolean,
    maxBytes: Long,
    onDismiss: () -> Unit,
    onReplaced: (String) -> Unit,
    viewModel: StudentDocumentsViewModel
) {
    val photo by viewModel.photo.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    LaunchedEffect(studentId) { viewModel.loadPhoto(studentId) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.replacePhoto(studentId, uri, maxBytes) { ok, msg ->
                onReplaced(msg)
                if (ok) { viewModel.reloadPhotoAfterReplace(studentId) }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Student Photo") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = appDimens().chartHeightPie), contentAlignment = Alignment.Center) {
                when {
                    photo.loading -> ThemedShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(appDimens().chartHeightPie),
                        shape = appDimens().cardShape
                    )
                    photo.error != null -> Text(photo.error!!, color = MaterialTheme.colorScheme.error)
                    else -> {
                        val img = rememberBase64ImageBitmap(photo.file?.base64)
                        if (img != null) {
                            Image(bitmap = img, contentDescription = "Student photo", modifier = Modifier.fillMaxWidth())
                        } else {
                            Text("No photo available")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (canReplace) {
                TextButton(onClick = { picker.launch("image/*") }, enabled = !busy) {
                    Text(if (busy) "Uploading…" else "Replace Photo")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun AadhaarViewerDialog(
    studentId: String,
    maskedNumber: String?,
    canReplace: Boolean,
    maxBytes: Long,
    onDismiss: () -> Unit,
    onReplaced: (String) -> Unit,
    viewModel: StudentDocumentsViewModel
) {
    val aadhaar by viewModel.aadhaar.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    LaunchedEffect(studentId) { viewModel.loadAadhaar(studentId) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.replaceAadhaar(studentId, uri, "image/jpeg", null, maxBytes) { ok, msg ->
                onReplaced(msg)
                if (ok) { viewModel.reloadAadhaarAfterReplace(studentId) }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.clearAadhaar(); onDismiss() },
        title = { Text("Aadhaar Document") },
        text = {
            Column {
                Text(
                    Formatters.maskAadhaar(maskedNumber).ifBlank { "Aadhaar number not set" },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(appDimens().spacingSm))
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = appDimens().chartHeightPie), contentAlignment = Alignment.Center) {
                    when {
                        aadhaar.loading -> ThemedShimmerBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(appDimens().chartHeightPie),
                            shape = appDimens().cardShape
                        )
                        aadhaar.error != null -> Text(aadhaar.error!!, color = MaterialTheme.colorScheme.error)
                        else -> {
                            val img = rememberBase64ImageBitmap(aadhaar.file?.base64)
                            if (img != null) {
                                Image(bitmap = img, contentDescription = "Aadhaar", modifier = Modifier.fillMaxWidth())
                            } else {
                                Text("No Aadhaar document available")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(appDimens().spacingSm))
                Text(
                    "This view has been logged for audit purposes.",
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors().warning
                )
            }
        },
        confirmButton = {
            if (canReplace) {
                TextButton(onClick = { picker.launch("image/*") }, enabled = !busy) {
                    Text(if (busy) "Uploading…" else "Replace Aadhaar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.clearAadhaar(); onDismiss() }) { Text("Close") }
        }
    )
}
