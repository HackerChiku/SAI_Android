package com.saicomputer.sms.feature.students

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.theme.StatusAmber

@Composable
private fun base64ToImage(base64: String?) = remember(base64) {
    base64?.let {
        runCatching {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
}

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
                if (ok) { viewModel.clearPhoto(); viewModel.loadPhoto(studentId) }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { viewModel.clearPhoto(); onDismiss() },
        title = { Text("Student Photo") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp), contentAlignment = Alignment.Center) {
                when {
                    photo.loading -> CircularProgressIndicator()
                    photo.error != null -> Text(photo.error!!, color = MaterialTheme.colorScheme.error)
                    else -> {
                        val img = base64ToImage(photo.file?.base64)
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
            TextButton(onClick = { viewModel.clearPhoto(); onDismiss() }) { Text("Close") }
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
                if (ok) { viewModel.clearAadhaar(); viewModel.loadAadhaar(studentId) }
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
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp), contentAlignment = Alignment.Center) {
                    when {
                        aadhaar.loading -> CircularProgressIndicator()
                        aadhaar.error != null -> Text(aadhaar.error!!, color = MaterialTheme.colorScheme.error)
                        else -> {
                            val img = base64ToImage(aadhaar.file?.base64)
                            if (img != null) {
                                Image(bitmap = img, contentDescription = "Aadhaar", modifier = Modifier.fillMaxWidth())
                            } else {
                                Text("No Aadhaar document available")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "This view has been logged for audit purposes.",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusAmber
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
