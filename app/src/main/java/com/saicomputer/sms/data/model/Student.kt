package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class StudentStatus { New, Active, PaymentPending, Completed, Dropout, NotTakenAdmission }

/** Subset settable manually via students.changeStatus. */
enum class ManualStudentStatus { Dropout, NotTakenAdmission, New }

val TERMINAL_STUDENT_STATUSES = setOf(StudentStatus.Dropout, StudentStatus.NotTakenAdmission)

enum class Gender { Male, Female, Other, PreferNotToSay }

enum class RegistrationSession { Before2017, After2017, NewRecord }

@Serializable
data class Student(
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
    @SerialName("Status") val status: StudentStatus,
    @SerialName("AdditionalNotes") val additionalNotes: String? = null,
    @SerialName("PhotoDriveID") val photoDriveId: String? = null,
    @SerialName("PhotoViewUrl") val photoViewUrl: String? = null,
    @SerialName("AadhaarNumber") val aadhaarNumber: String? = null,
    @SerialName("AadhaarPhotoDriveID") val aadhaarPhotoDriveId: String? = null,
    @SerialName("AadhaarPhotoViewUrl") val aadhaarPhotoViewUrl: String? = null,
    @SerialName("RegistrationSession") val registrationSession: RegistrationSession = RegistrationSession.NewRecord,
    @SerialName("OldRegistrationNumber") val oldRegistrationNumber: String? = null,
    @SerialName("EffectiveCreatedAt") val effectiveCreatedAt: String? = null,
    @SerialName("IsBackdate") val isBackdate: Boolean = false,
    @SerialName("CreatedAt") val createdAt: String = "",
    @SerialName("UpdatedAt") val updatedAt: String = ""
)
