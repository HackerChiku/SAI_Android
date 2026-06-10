package com.saicomputer.sms.feature.students

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.media.ImageCompressor
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.dto.FileUploadInput
import com.saicomputer.sms.data.model.FileBase64Response
import com.saicomputer.sms.data.model.StudentDocumentType
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

    private var activePhotoStudentId: String = ""
    private var activeAadhaarStudentId: String = ""

    fun loadPhoto(studentId: String) {
        activePhotoStudentId = studentId
        val cached = repository.getCachedDocument(studentId, StudentDocumentType.Photo)
        if (cached != null) {
            _photo.value = FileViewState(file = cached)
            refreshPhotoSilently(studentId)
        } else {
            fetchPhoto(studentId)
        }
    }

    private fun fetchPhoto(studentId: String) {
        activePhotoStudentId = studentId
        _photo.value = FileViewState(loading = true)
        viewModelScope.launch {
            try {
                val file = repository.refreshPhoto(studentId)
                if (activePhotoStudentId == studentId) {
                    _photo.value = FileViewState(file = file)
                }
            } catch (e: ApiException) {
                if (activePhotoStudentId == studentId) {
                    _photo.value = FileViewState(error = e.friendlyMessage())
                }
            }
        }
    }

    private fun refreshPhotoSilently(studentId: String) {
        viewModelScope.launch {
            runCatching { repository.refreshPhoto(studentId) }
                .onSuccess { file ->
                    if (activePhotoStudentId == studentId) {
                        _photo.value = FileViewState(file = file)
                    }
                }
        }
    }

    /** Clears VM state only; repository session cache is preserved. */
    fun clearPhoto() {
        activePhotoStudentId = ""
        _photo.value = FileViewState()
    }

    fun loadAadhaar(studentId: String) {
        activeAadhaarStudentId = studentId
        val cached = repository.getCachedDocument(studentId, StudentDocumentType.Aadhaar)
        if (cached != null) {
            _aadhaar.value = FileViewState(file = cached)
            refreshAadhaarSilently(studentId)
        } else {
            fetchAadhaar(studentId)
        }
    }

    private fun fetchAadhaar(studentId: String) {
        activeAadhaarStudentId = studentId
        _aadhaar.value = FileViewState(loading = true)
        viewModelScope.launch {
            try {
                val file = repository.refreshAadhaar(studentId)
                if (activeAadhaarStudentId == studentId) {
                    _aadhaar.value = FileViewState(file = file)
                }
            } catch (e: ApiException) {
                if (activeAadhaarStudentId == studentId) {
                    _aadhaar.value = FileViewState(error = e.friendlyMessage())
                }
            }
        }
    }

    private fun refreshAadhaarSilently(studentId: String) {
        viewModelScope.launch {
            runCatching { repository.refreshAadhaar(studentId) }
                .onSuccess { file ->
                    if (activeAadhaarStudentId == studentId) {
                        _aadhaar.value = FileViewState(file = file)
                    }
                }
        }
    }

    /** Clears VM state only; repository session cache is preserved. */
    fun clearAadhaar() {
        activeAadhaarStudentId = ""
        _aadhaar.value = FileViewState()
    }

    fun reloadPhotoAfterReplace(studentId: String) {
        repository.invalidateDocument(studentId, StudentDocumentType.Photo)
        fetchPhoto(studentId)
    }

    fun reloadAadhaarAfterReplace(studentId: String) {
        repository.invalidateDocument(studentId, StudentDocumentType.Aadhaar)
        fetchAadhaar(studentId)
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
