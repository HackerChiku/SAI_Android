package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionListItem(
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("StudentID") val studentId: String,
    @SerialName("StudentName") val studentName: String,
    @SerialName("StudentPhone") val studentPhone: String = "",
    @SerialName("CourseID") val courseId: String,
    @SerialName("CourseName") val courseName: String,
    @SerialName("StartDate") val startDate: String,
    @SerialName("ExpectedEndDate") val expectedEndDate: String,
    @SerialName("ActualEndDate") val actualEndDate: String? = null,
    @SerialName("EnrollmentStatus") val enrollmentStatus: EnrollmentStatus,
    @SerialName("CompletionTrigger") val completionTrigger: String? = null,
    @SerialName("MonthlyFee") val monthlyFee: Int,
    @SerialName("PackageType") val packageType: PackageType = PackageType.NONE,
    @SerialName("PaidThroughDate") val paidThroughDate: String? = null,
    @SerialName("TotalAmountPaid") val totalAmountPaid: Int = 0,
    @SerialName("IsPendingCurrentMonth") val isPendingCurrentMonth: Boolean = false
)
