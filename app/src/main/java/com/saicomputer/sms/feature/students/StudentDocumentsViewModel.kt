package com.saicomputer.sms.feature.students

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.media.ImageCompressor
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.dto.FileUploadInput
import com.saicomputer.sms.data.model.FileBase64Response
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FileViewState(
    val loading: Boolean = false,
    val file: FileBase64Response? = null,
    val error: String? = null
)

@HiltViewModel
class StudentDocumentsViewModel @Inject constructor(
    private val repository: StudentsRepository,
    private val compressor: ImageCompressor
) : ViewModel() {

    private val _photo = MutableStateFlow(FileViewState())
    val photo: StateFlow<FileViewState> = _photo.asStateFlow()

    private val _aadhaar = MutableStateFlow(FileViewState())
    val aadhaar: StateFlow<FileViewState> = _aadhaar.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** Fetch photo base64 on demand (viewer open). */
    fun loadPhoto(studentId: String) {
        _photo.value = FileViewState(loading = true)
        viewModelScope.launch {
            try {
                _photo.value = FileViewState(file = repository.getPhotoBase64(studentId))
            } catch (e: ApiException) {
                _photo.value = FileViewState(error = e.friendlyMessage())
            }
        }
    }

    fun clearPhoto() {
        _photo.value = FileViewState()
    }

    /** Fetch Aadhaar base64 ON OPEN (audit-logged); discard on close. */
    fun loadAadhaar(studentId: String) {
        _aadhaar.value = FileViewState(loading = true)
        viewModelScope.launch {
            try {
                _aadhaar.value = FileViewState(file = repository.getAadhaarBase64(studentId))
            } catch (e: ApiException) {
                _aadhaar.value = FileViewState(error = e.friendlyMessage())
            }
        }
    }

    fun clearAadhaar() {
        _aadhaar.value = FileViewState()
    }

    fun replacePhoto(
        studentId: String,
        uri: Uri,
        maxBytes: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        _busy.value = true
        viewModelScope.launch {
            try {
                val size = compressor.sizeOf(uri)
                if (size in 1..maxBytes || size == -1L) {
                    val prepared = compressor.compressImage(uri)
                    repository.replacePhoto(
                        FileUploadInput(studentId, prepared.base64, prepared.mimeType)
                    )
                    onResult(true, "Photo replaced")
                } else {
                    onResult(false, "Image exceeds the allowed size")
                }
            } catch (e: Exception) {
                onResult(false, (e as? ApiException)?.friendlyMessage() ?: (e.message ?: "Failed"))
            } finally {
                _busy.value = false
            }
        }
    }

    fun replaceAadhaar(
        studentId: String,
        uri: Uri,
        mimeType: String,
        reason: String?,
        maxBytes: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        _busy.value = true
        viewModelScope.launch {
            try {
                val prepared = if (mimeType == "application/pdf") {
                    compressor.readAsBase64(uri)
                } else {
                    compressor.compressImage(uri)
                }
                if (prepared.sizeBytes > maxBytes) {
                    onResult(false, "File exceeds the allowed size")
                } else {
                    repository.replaceAadhaar(
                        FileUploadInput(studentId, prepared.base64, prepared.mimeType, reason)
                    )
                    onResult(true, "Aadhaar replaced")
                }
            } catch (e: Exception) {
                onResult(false, (e as? ApiException)?.friendlyMessage() ?: (e.message ?: "Failed"))
            } finally {
                _busy.value = false
            }
        }
    }
}
