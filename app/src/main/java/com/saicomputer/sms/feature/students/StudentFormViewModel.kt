package com.saicomputer.sms.feature.students

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.media.ImageCompressor
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.core.validation.Validators
import com.saicomputer.sms.data.dto.FileUploadInput
import com.saicomputer.sms.data.dto.StudentCreateInput
import com.saicomputer.sms.data.dto.StudentUpdateInput
import com.saicomputer.sms.data.model.Gender
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentFormState(
    val isEdit: Boolean = false,
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val address: String = "",
    val academicQualification: String = "",
    val lastInstitution: String = "",
    val additionalNotes: String = "",
    val aadhaarNumber: String = "",
    val aadhaarLoading: Boolean = false,
    val registrationSession: RegistrationSession = RegistrationSession.NewRecord,
    val oldRegistrationNumber: String = "",
    val backdateEnabled: Boolean = false,
    val effectiveCreatedAt: String? = null,
    val photoFileLabel: String? = null,
    val aadhaarFileLabel: String? = null,
    val error: String? = null
) {
    val nameError: String? get() = if (fullName.isNotBlank()) null else null
    val phoneError: String? get() = Validators.phone(phoneNumber).takeIf { phoneNumber.isNotBlank() }
    val emailError: String? get() = Validators.email(email).takeIf { email.isNotBlank() }
    val aadhaarError: String? get() = Validators.aadhaar(aadhaarNumber.ifBlank { null })
    val oldRegError: String?
        get() = if (registrationSession != RegistrationSession.NewRecord && oldRegistrationNumber.isBlank()) {
            "Old registration number is required for legacy records"
        } else null

    val canSubmit: Boolean
        get() = fullName.isNotBlank() &&
            Validators.phone(phoneNumber) == null &&
            emailError == null &&
            aadhaarError == null &&
            oldRegError == null &&
            !submitting &&
            !aadhaarLoading &&
            !loading
}

@HiltViewModel
class StudentFormViewModel @Inject constructor(
    private val repository: StudentsRepository,
    private val compressor: ImageCompressor,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(StudentFormState())
    val state: StateFlow<StudentFormState> = _state.asStateFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    private var studentId: String? = null
    private var pendingPhotoUri: Uri? = null
    private var pendingAadhaarUri: Uri? = null

    fun initialize(id: String?) {
        if (studentId == id && (_state.value.isEdit || id == null)) return
        studentId = id
        if (id == null) {
            _state.value = StudentFormState(isEdit = false)
        } else {
            _state.value = StudentFormState(isEdit = true, loading = true)
            loadStudent(id)
        }
    }

    private fun loadStudent(id: String) {
        viewModelScope.launch {
            try {
                val student = repository.get(id).student
                _state.update {
                    it.copy(
                        loading = false,
                        fullName = student.fullName,
                        phoneNumber = student.phoneNumber,
                        email = student.email.orEmpty(),
                        parentName = student.parentName.orEmpty(),
                        parentPhone = student.parentPhone.orEmpty(),
                        dateOfBirth = student.dateOfBirth,
                        gender = student.gender,
                        address = student.address.orEmpty(),
                        academicQualification = student.academicQualification.orEmpty(),
                        lastInstitution = student.lastInstitution.orEmpty(),
                        additionalNotes = student.additionalNotes.orEmpty(),
                        registrationSession = student.registrationSession,
                        oldRegistrationNumber = student.oldRegistrationNumber.orEmpty()
                    )
                }
                // v12: fetch the REAL Aadhaar number for editing (uncached, audit-logged).
                fetchRealAadhaar(id)
            } catch (e: ApiException) {
                _state.update { it.copy(loading = false, error = e.friendlyMessage()) }
            }
        }
    }

    private fun fetchRealAadhaar(id: String) {
        _state.update { it.copy(aadhaarLoading = true) }
        viewModelScope.launch {
            try {
                val res = repository.getAadhaarNumber(id)
                _state.update { it.copy(aadhaarLoading = false, aadhaarNumber = res.aadhaarNumber.orEmpty()) }
            } catch (e: Exception) {
                // On failure, keep field empty but allow editing other fields.
                _state.update { it.copy(aadhaarLoading = false) }
            }
        }
    }

    fun update(transform: (StudentFormState) -> StudentFormState) = _state.update(transform)

    fun onAadhaarChange(value: String) {
        _state.update { it.copy(aadhaarNumber = Validators.digitsOnly(value, 12)) }
    }

    fun onRegistrationSessionChange(session: RegistrationSession) {
        _state.update {
            it.copy(
                registrationSession = session,
                oldRegistrationNumber = if (session == RegistrationSession.NewRecord) "" else it.oldRegistrationNumber
            )
        }
    }

    fun onGenderChange(gender: Gender?) {
        _state.update { it.copy(gender = gender) }
    }

    fun onDateOfBirthChange(value: String) {
        _state.update { it.copy(dateOfBirth = value) }
    }

    fun onPhotoPicked(uri: Uri?, label: String?) {
        pendingPhotoUri = uri
        _state.update { it.copy(photoFileLabel = label) }
    }

    fun onAadhaarPicked(uri: Uri?, label: String?) {
        pendingAadhaarUri = uri
        _state.update { it.copy(aadhaarFileLabel = label) }
    }

    fun submit(onSaved: (String) -> Unit) {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                val id = if (s.isEdit) {
                    val updatedId = repository.update(
                        StudentUpdateInput(
                            studentId = studentId!!,
                            fullName = s.fullName.trim(),
                            phoneNumber = s.phoneNumber.filter { c -> c.isDigit() },
                            email = s.email.ifBlank { null },
                            parentName = s.parentName.ifBlank { null },
                            parentPhone = s.parentPhone.ifBlank { null }?.filter { c -> c.isDigit() },
                            dateOfBirth = s.dateOfBirth,
                            gender = s.gender,
                            address = s.address.ifBlank { null },
                            academicQualification = s.academicQualification.ifBlank { null },
                            lastInstitution = s.lastInstitution.ifBlank { null },
                            additionalNotes = s.additionalNotes.ifBlank { null },
                            aadhaarNumber = s.aadhaarNumber.ifBlank { null },
                            registrationSession = s.registrationSession,
                            oldRegistrationNumber = s.oldRegistrationNumber.ifBlank { null }
                        )
                    ).studentId
                    uploadPendingDocuments(updatedId)
                    updatedId
                } else {
                    val createdId = repository.create(
                        StudentCreateInput(
                            fullName = s.fullName.trim(),
                            phoneNumber = s.phoneNumber.filter { c -> c.isDigit() },
                            email = s.email.ifBlank { null },
                            parentName = s.parentName.ifBlank { null },
                            parentPhone = s.parentPhone.ifBlank { null }?.filter { c -> c.isDigit() },
                            dateOfBirth = s.dateOfBirth,
                            gender = s.gender,
                            address = s.address.ifBlank { null },
                            academicQualification = s.academicQualification.ifBlank { null },
                            lastInstitution = s.lastInstitution.ifBlank { null },
                            additionalNotes = s.additionalNotes.ifBlank { null },
                            aadhaarNumber = s.aadhaarNumber.ifBlank { null },
                            registrationSession = s.registrationSession,
                            oldRegistrationNumber = s.oldRegistrationNumber.ifBlank { null },
                            effectiveCreatedAt = if (s.backdateEnabled) s.effectiveCreatedAt else null
                        )
                    ).studentId
                    uploadPendingDocuments(createdId)
                    createdId
                }
                _state.update { it.copy(submitting = false) }
                onSaved(id)
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    private suspend fun uploadPendingDocuments(studentId: String) {
        pendingPhotoUri?.let { uri ->
            val prepared = compressor.compressImage(uri)
            if (prepared.sizeBytes <= PHOTO_MAX_BYTES) {
                repository.replacePhoto(FileUploadInput(studentId, prepared.base64, prepared.mimeType))
            }
        }
        pendingAadhaarUri?.let { uri ->
            val prepared = compressor.compressImage(uri)
            if (prepared.sizeBytes <= AADHAAR_MAX_BYTES) {
                repository.replaceAadhaar(FileUploadInput(studentId, prepared.base64, prepared.mimeType))
            }
        }
        pendingPhotoUri = null
        pendingAadhaarUri = null
    }

    companion object {
        const val PHOTO_MAX_BYTES = 2L * 1024 * 1024
        const val AADHAAR_MAX_BYTES = 5L * 1024 * 1024
    }
}
