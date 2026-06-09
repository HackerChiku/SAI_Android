package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.data.model.InstallmentType
import com.saicomputer.sms.data.model.Payment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentListFilters(
    @SerialName("StudentID") val studentId: String = "",
    @SerialName("CourseID") val courseId: String = "",
    val status: String = "",
    val limit: Int = 100,
    val offset: Int = 0
)

@Serializable
data class EnrollmentListResponse(
    val total: Int = 0,
    val offset: Int = 0,
    val limit: Int = 100,
    val rows: List<Enrollment> = emptyList()
)

@Serializable
data class EnrollmentPreviewInput(
    val studentId: String,
    val courseId: String,
    val startDate: String,
    val installmentType: InstallmentType,
    val effectiveFee: Int? = null,
    val effectiveEnrollmentFee: Int? = null,
    val requestedCount: Int? = null
)

@Serializable
data class PreviewRow(
    val installmentNumber: Int,
    val amountDue: Int,
    val dueDate: String
)

@Serializable
data class EnrollmentPreviewResult(
    val courseId: String = "",
    @SerialName("MaxInstallments") val maxInstallments: Int = 12,
    @SerialName("InstallmentType") val installmentType: InstallmentType? = null,
    @SerialName("TotalAmountDue") val totalAmount: Int = 0,
    @SerialName("Installments") val installments: List<PreviewRow> = emptyList(),
    @SerialName("EnrollmentFeeWaived") val enrollmentFeeWaived: Boolean = false,
    @SerialName("WaivedFromCourseName") val waivedFromCourseName: String? = null,
    @SerialName("SuggestedEnrollmentFee") val suggestedEnrollmentFee: Int = 0,
    @SerialName("SuggestedExpectedEndDate") val suggestedExpectedEndDate: String? = null,
    @SerialName("ExpectedEndDate") val expectedEndDate: String? = null
)

/** Row used when submitting a new installment schedule on create. */
@Serializable
data class InstallmentCreateRow(
    val amountDue: Int,
    val dueDate: String
)

/**
 * Typed (non-serialized) sealed input. The repository branches on the concrete
 * type and serializes the matching @Serializable payload.
 */
sealed interface EnrollmentCreateInput {
    val billingType: BillingType
    val isBackdate: Boolean?
    val effectiveCreatedAt: String?
}

@Serializable
data class InstallmentEnrollmentCreateInput(
    val studentId: String,
    val courseId: String,
    val startDate: String,
    @SerialName("EffectiveFee") val effectiveFee: Int,
    @SerialName("EffectiveEnrollmentFee") val effectiveEnrollmentFee: Int,
    val installmentType: InstallmentType,
    val installments: List<InstallmentCreateRow>,
    override val billingType: BillingType = BillingType.Installment,
    override val isBackdate: Boolean? = null,
    override val effectiveCreatedAt: String? = null
) : EnrollmentCreateInput

@Serializable
data class SubscriptionEnrollmentCreateInput(
    val studentId: String,
    val courseId: String,
    val startDate: String,
    val expectedEndDate: String,
    val effectiveMonthlyFee: Int,
    val effectiveEnrollmentFee: Int,
    override val billingType: BillingType = BillingType.Subscription,
    override val isBackdate: Boolean? = null,
    override val effectiveCreatedAt: String? = null
) : EnrollmentCreateInput

/**
 * enrollments.get returns the enrollment flat (with nested student/course/installments/
 * topics) and a `payments` array. The repository assembles this wrapper.
 */
data class EnrollmentGetResponse(
    val enrollment: Enrollment,
    val payments: List<Payment>? = null
)

@Serializable
data class MarkCompleteInput(
    @SerialName("EnrollmentID") val enrollmentId: String,
    val forceComplete: Boolean? = null,
    val isBackdate: Boolean? = null,
    val effectiveActualEndDate: String? = null
)

@Serializable
data class IncompleteTopic(
    val enrollmentTopicId: String,
    val topicName: String = ""
)

@Serializable
data class IncompleteTopicsDetails(
    val totalTopics: Int = 0,
    val completedTopics: Int = 0,
    val incompleteTopics: List<IncompleteTopic> = emptyList()
)

@Serializable
data class MarkCompleteResponse(
    val requiresConfirmation: Boolean = false,
    val warning: String? = null,
    val message: String? = null,
    val details: IncompleteTopicsDetails? = null,
    val enrollment: Enrollment? = null
)

@Serializable
data class CancelEnrollmentInput(
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("Reason") val reason: String? = null
)

@Serializable
data class SetExcludedFromBillingInput(
    @SerialName("EnrollmentID") val enrollmentId: String,
    val excluded: Boolean,
    val reason: String? = null
)

@Serializable
data class SetExcludedFromBillingResult(
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("ExcludedFromBilling") val excludedFromBilling: Boolean,
    @SerialName("NoChange") val noChange: Boolean = false
)

@Serializable
data class InstallmentEditRow(
    val installmentId: String? = null,
    val amountDue: Int,
    val dueDate: String
)

@Serializable
data class EditInstallmentsInput(
    val enrollmentId: String,
    val installments: List<InstallmentEditRow>
)

@Serializable
data class EditInstallmentsResponse(
    val ok: Boolean = true,
    val enrollmentId: String = "",
    val installments: List<Installment>? = null
)

// ---- Enrollment topics ----
@Serializable
data class EnrollmentTopicsListResponse(
    val topics: List<com.saicomputer.sms.data.model.EnrollmentTopic> = emptyList(),
    val summary: com.saicomputer.sms.data.model.TopicsSummary? = null
)

@Serializable
data class MarkTopicCompleteInput(
    val enrollmentTopicId: String,
    val notes: String? = null
)

@Serializable
data class UnmarkTopicInput(
    val enrollmentTopicId: String
)
