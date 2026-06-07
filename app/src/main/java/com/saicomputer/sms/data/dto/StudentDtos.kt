package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Gender
import com.saicomputer.sms.data.model.ManualStudentStatus
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.StudentStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** students.list filters — PascalCase except registrationSession (per API doc). */
@Serializable
data class StudentListFilters(
    @SerialName("Search") val search: String? = null,
    @SerialName("Status") val status: String? = null,
    @SerialName("registrationSession") val registrationSession: String? = null,
    @SerialName("Limit") val limit: Int = 50,
    @SerialName("Offset") val offset: Int = 0
)

@Serializable
data class StudentListResponse(
    val total: Int = 0,
    val offset: Int = 0,
    val limit: Int = 50,
    val rows: List<Student> = emptyList()
)

/**
 * students.get returns the student DTO flat plus an `enrollments` array (no wrapper).
 * The repository decodes the flat student and the enrollments separately and assembles
 * this. `payments` is filled by a follow-up payments.list call.
 */
data class StudentGetResponse(
    val student: Student,
    val enrollments: List<Enrollment>? = null,
    val payments: List<Payment>? = null
)

@Serializable
data class StudentCreateInput(
    @SerialName("FullName") val fullName: String,
    @SerialName("PhoneNumber") val phoneNumber: String,
    @SerialName("Email") val email: String? = null,
    @SerialName("ParentName") val parentName: String? = null,
    @SerialName("ParentPhone") val parentPhone: String? = null,
    @SerialName("DateOfBirth") val dateOfBirth: String? = null,
    @SerialName("Gender") val gender: Gender? = null,
    @SerialName("Address") val address: String? = null,
    @SerialName("AcademicQualification") val academicQualification: String? = null,
    @SerialName("LastInstitution") val lastInstitution: String? = null,
    @SerialName("AdditionalNotes") val additionalNotes: String? = null,
    @SerialName("AadhaarNumber") val aadhaarNumber: String? = null,
    @SerialName("RegistrationSession") val registrationSession: RegistrationSession? = null,
    @SerialName("OldRegistrationNumber") val oldRegistrationNumber: String? = null,
    @SerialName("EffectiveCreatedAt") val effectiveCreatedAt: String? = null
)

@Serializable
data class StudentUpdateInput(
    @SerialName("StudentID") val studentId: String,
    @SerialName("FullName") val fullName: String,
    @SerialName("PhoneNumber") val phoneNumber: String,
    @SerialName("Email") val email: String? = null,
    @SerialName("ParentName") val parentName: String? = null,
    @SerialName("ParentPhone") val parentPhone: String? = null,
    @SerialName("DateOfBirth") val dateOfBirth: String? = null,
    @SerialName("Gender") val gender: Gender? = null,
    @SerialName("Address") val address: String? = null,
    @SerialName("AcademicQualification") val academicQualification: String? = null,
    @SerialName("LastInstitution") val lastInstitution: String? = null,
    @SerialName("AdditionalNotes") val additionalNotes: String? = null,
    @SerialName("AadhaarNumber") val aadhaarNumber: String? = null,
    @SerialName("RegistrationSession") val registrationSession: RegistrationSession? = null,
    @SerialName("OldRegistrationNumber") val oldRegistrationNumber: String? = null
)

@Serializable
data class ChangeStudentStatusInput(
    @SerialName("StudentID") val studentId: String,
    @SerialName("NewStatus") val newStatus: ManualStudentStatus,
    @SerialName("Reason") val reason: String? = null
)

@Serializable
data class ChangeStudentStatusResponse(
    @SerialName("StudentID") val studentId: String,
    @SerialName("OldStatus") val oldStatus: StudentStatus? = null,
    @SerialName("NewStatus") val newStatus: StudentStatus? = null,
    @SerialName("Status") val status: StudentStatus? = null,
    @SerialName("CascadeCancelledEnrollments") val cascadeCancelledEnrollments: List<String> = emptyList(),
    @SerialName("NoChange") val noChange: Boolean = false
)

@Serializable
data class AadhaarNumberResponse(
    @SerialName("AadhaarNumber") val aadhaarNumber: String? = null
)

@Serializable
data class FileUploadInput(
    val studentId: String,
    val fileBase64: String,
    val mimeType: String,
    val reason: String? = null
)

@Serializable
data class ReplacePhotoResponse(
    val photoDriveId: String? = null,
    val photoViewUrl: String? = null,
    val historyId: String? = null
)

@Serializable
data class ReplaceAadhaarResponse(
    val aadhaarPhotoDriveId: String? = null,
    val aadhaarPhotoViewUrl: String? = null,
    val archivedDriveId: String? = null,
    val historyId: String? = null,
    val prunedCount: Int = 0
)
