package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class BillingType { Installment, Subscription }

@Serializable
enum class PackageType {
    @SerialName("None") NONE,
    @SerialName("3+1") PACKAGE_3_1
}

enum class InstallmentType { Monthly, Quarterly, Custom }

@Serializable
data class Course(
    @SerialName("CourseID") val courseId: String,
    @SerialName("CourseName") val courseName: String,
    @SerialName("CourseFullName") val courseFullName: String,
    @SerialName("Description") val description: String? = null,
    @SerialName("CourseLink") val courseLink: String? = null,
    @SerialName("DurationMonths") val durationMonths: Int,
    @SerialName("Fee") val fee: Int,
    @SerialName("EnrollmentFee") val enrollmentFee: Int,
    @SerialName("MaxInstallments") val maxInstallments: Int = 12,
    @SerialName("BillingType") val billingType: BillingType = BillingType.Installment,
    @SerialName("MonthlyFee") val monthlyFee: Int = 0,
    @SerialName("PackageType") val packageType: PackageType = PackageType.NONE,
    @SerialName("PackagePaidMonths") val packagePaidMonths: Int? = null,
    @SerialName("PackageBonusMonths") val packageBonusMonths: Int? = null,
    @SerialName("Category") val category: String? = null,
    @SerialName("GenerateCertificate") val generateCertificate: Boolean = false,
    @SerialName("HasTopics") val hasTopics: Boolean = false,
    @SerialName("TopicsCount") val topicsCount: Int? = null,
    @SerialName("IsActive") val isActive: Boolean = true,
    @SerialName("CurrentActiveEnrollments") val currentActiveEnrollments: Int = 0
)
