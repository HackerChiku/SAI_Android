package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("EnumEntryName")
enum class DashboardPeriod { thisMonth, lastMonth, lastQuarter, lastHalfYear, lastYear }

@Serializable
data class DashboardRange(val start: String, val end: String)

@Serializable
data class PaymentMethodBreakdown(
    val UPI: Int = 0,
    val CASH: Int = 0,
    val QR: Int = 0
)

@Serializable
data class MonthlyRevenuePoint(val month: String, val amount: Int)

@Serializable
data class DashboardSummaryResponse(
    val range: DashboardRange,
    val totalNewEnrollments: Int = 0,
    val totalAdmission: Int = 0,
    val totalFeeCollected: Int = 0,
    val totalFeeDueThisMonth: Int = 0,
    val paymentPendingThisMonth: Int = 0,
    val activeEnrollments: Int = 0,
    val completedThisPeriod: Int = 0,
    val paymentMethodBreakdown: PaymentMethodBreakdown = PaymentMethodBreakdown(),
    val monthlyRevenueTrend: List<MonthlyRevenuePoint> = emptyList()
)

@Serializable
data class DashboardPendingStudent(
    @SerialName("StudentID") val studentId: String,
    @SerialName("FullName") val fullName: String,
    @SerialName("PhoneNumber") val phoneNumber: String,
    @SerialName("Email") val email: String = "",
    val totalPendingThisMonth: Int = 0
)

@Serializable
data class DashboardPendingStudentsResponse(
    val monthRange: DashboardRange,
    val total: Int = 0,
    val students: List<DashboardPendingStudent> = emptyList()
)
