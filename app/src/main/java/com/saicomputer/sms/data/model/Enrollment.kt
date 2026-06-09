package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class InstallmentStatus { Unpaid, Partial, Paid, Overdue }

@Serializable
data class Installment(
    @SerialName("InstallmentID") val installmentId: String,
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("InstallmentNumber") val installmentNumber: Int,
    @SerialName("AmountDue") val amountDue: Int,
    @SerialName("DueDate") val dueDate: String,
    @SerialName("AmountPaid") val amountPaid: Int = 0,
    @SerialName("Status") val status: InstallmentStatus = InstallmentStatus.Unpaid,
    @SerialName("Balance") val balance: Int = 0
)

enum class EnrollmentStatus { Ongoing, Completed, Cancelled }
enum class CertificateStatus { NotApplicable, NotEligible, Eligible, Generated, Emailed }

/** Lightweight student join returned inside enrollments.get. */
@Serializable
data class EnrollmentStudentRef(
    @SerialName("StudentID") val studentId: String? = null,
    @SerialName("FullName") val fullName: String? = null,
    @SerialName("PhoneNumber") val phoneNumber: String? = null
)

/** Lightweight course join returned inside enrollments.get. */
@Serializable
data class EnrollmentCourseRef(
    @SerialName("CourseID") val courseId: String? = null,
    @SerialName("CourseName") val courseName: String? = null,
    @SerialName("CourseFullName") val courseFullName: String? = null,
    @SerialName("HasTopics") val hasTopics: Boolean = false,
    @SerialName("MaxInstallments") val maxInstallments: Int? = null,
    @SerialName("BillingType") val billingType: BillingType? = null,
    @SerialName("MonthlyFee") val monthlyFee: Int? = null,
    @SerialName("PackageType") val packageType: PackageType? = null
)

@Serializable
data class Enrollment(
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("StudentID") val studentId: String? = null,
    @SerialName("CourseID") val courseId: String,
    @SerialName("StartDate") val startDate: String,
    @SerialName("ExpectedEndDate") val expectedEndDate: String,
    @SerialName("ActualEndDate") val actualEndDate: String? = null,
    @SerialName("EnrollmentStatus") val enrollmentStatus: EnrollmentStatus,
    @SerialName("BillingType") val billingType: BillingType? = null,
    @SerialName("TotalAmountDue") val totalAmountDue: Int,
    @SerialName("TotalAmountPaid") val totalAmountPaid: Int,
    @SerialName("Balance") val balance: Int = 0,
    @SerialName("EffectiveFee") val effectiveFee: Int = 0,
    @SerialName("EffectiveEnrollmentFee") val effectiveEnrollmentFee: Int = 0,
    @SerialName("InstallmentType") val installmentType: InstallmentType? = null,
    @SerialName("MonthlyFee") val monthlyFee: Int? = null,
    @SerialName("PackageType") val packageType: PackageType? = null,
    @SerialName("PackagePaidMonths") val packagePaidMonths: Int? = null,
    @SerialName("PackageBonusMonths") val packageBonusMonths: Int? = null,
    @SerialName("PaidThroughDate") val paidThroughDate: String? = null,
    @SerialName("PaidThroughEndDate") val paidThroughEndDate: String? = null,
    @SerialName("IsPendingCurrentMonth") val isPendingCurrentMonth: Boolean = false,
    @SerialName("ExcludedFromBilling") val excludedFromBilling: Boolean = false,
    @SerialName("EffectiveCreatedAt") val effectiveCreatedAt: String? = null,
    @SerialName("IsBackdate") val isBackdate: Boolean = false,
    @SerialName("CertificateStatus") val certificateStatus: CertificateStatus = CertificateStatus.NotApplicable,
    @SerialName("CertificateID") val certificateId: String? = null,
    val installmentsLocked: Boolean = false,
    val enrollmentFeeWaived: Boolean = false,
    val waivedFromCourseName: String? = null,
    val student: EnrollmentStudentRef? = null,
    val course: EnrollmentCourseRef? = null,
    val installments: List<Installment>? = null,
    val topics: List<EnrollmentTopic>? = null,
    val topicsSummary: TopicsSummary? = null,
    /** Flat join fields returned on some list endpoints. */
    @SerialName("StudentName") val studentNameField: String? = null,
    @SerialName("CourseName") val courseNameField: String? = null,
    @SerialName("CourseFullName") val courseFullNameField: String? = null
) {
    /** Convenience accessors from nested joins or flat list fields. */
    val courseName: String? get() = course?.courseName ?: courseNameField
    val courseFullName: String? get() = course?.courseFullName ?: courseFullNameField
    val studentName: String? get() = student?.fullName ?: studentNameField
}
