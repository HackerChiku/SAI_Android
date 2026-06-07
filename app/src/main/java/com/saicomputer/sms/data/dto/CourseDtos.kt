package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.CourseTopic
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.TopicDurationUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseListResponse(
    val rows: List<Course> = emptyList(),
    val total: Int = 0
)

/** courses.get returns the course DTO flat; the repository decodes it and wraps here. */
data class CourseGetResponse(val course: Course)

@Serializable
data class CourseCreateInput(
    @SerialName("CourseName") val courseName: String,
    @SerialName("CourseFullName") val courseFullName: String,
    @SerialName("Description") val description: String? = null,
    @SerialName("CourseLink") val courseLink: String? = null,
    @SerialName("DurationMonths") val durationMonths: Int,
    @SerialName("Fee") val fee: Int,
    @SerialName("EnrollmentFee") val enrollmentFee: Int,
    @SerialName("MaxInstallments") val maxInstallments: Int = 12,
    @SerialName("BillingType") val billingType: BillingType,
    @SerialName("MonthlyFee") val monthlyFee: Int = 0,
    @SerialName("PackageType") val packageType: PackageType = PackageType.NONE,
    @SerialName("PackagePaidMonths") val packagePaidMonths: Int? = null,
    @SerialName("PackageBonusMonths") val packageBonusMonths: Int? = null,
    @SerialName("Category") val category: String? = null,
    @SerialName("GenerateCertificate") val generateCertificate: Boolean = false,
    @SerialName("HasTopics") val hasTopics: Boolean = false,
    @SerialName("IsActive") val isActive: Boolean = true
)

@Serializable
data class CourseUpdateInput(
    @SerialName("CourseID") val courseId: String,
    @SerialName("CourseName") val courseName: String,
    @SerialName("CourseFullName") val courseFullName: String,
    @SerialName("Description") val description: String? = null,
    @SerialName("CourseLink") val courseLink: String? = null,
    @SerialName("DurationMonths") val durationMonths: Int,
    @SerialName("Fee") val fee: Int,
    @SerialName("EnrollmentFee") val enrollmentFee: Int,
    @SerialName("MaxInstallments") val maxInstallments: Int = 12,
    @SerialName("BillingType") val billingType: BillingType,
    @SerialName("MonthlyFee") val monthlyFee: Int = 0,
    @SerialName("PackageType") val packageType: PackageType = PackageType.NONE,
    @SerialName("PackagePaidMonths") val packagePaidMonths: Int? = null,
    @SerialName("PackageBonusMonths") val packageBonusMonths: Int? = null,
    @SerialName("Category") val category: String? = null,
    @SerialName("GenerateCertificate") val generateCertificate: Boolean = false,
    @SerialName("HasTopics") val hasTopics: Boolean = false,
    @SerialName("IsActive") val isActive: Boolean = true
)

@Serializable
data class CourseTopicRow(
    val courseTopicId: String? = null,
    val topicName: String,
    val description: String? = null,
    val estimatedDurationValue: Int,
    val estimatedDurationUnit: TopicDurationUnit
)

@Serializable
data class BulkSaveCourseTopicsInput(
    val courseId: String,
    val topics: List<CourseTopicRow>
)

@Serializable
data class CourseTopicsListResponse(val topics: List<CourseTopic> = emptyList())
