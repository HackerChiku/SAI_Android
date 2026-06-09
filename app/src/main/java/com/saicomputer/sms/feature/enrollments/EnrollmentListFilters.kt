package com.saicomputer.sms.feature.enrollments

import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.EnrollmentStatus

object EnrollmentDisplayStatus {
    const val ALL = "All"
    const val ACTIVE = "Active"
    const val PAYMENT_PENDING = "PaymentPending"
    const val COMPLETED = "Completed"
    const val CANCELLED = "Cancelled"

    val OPTIONS = listOf(ALL, ACTIVE, PAYMENT_PENDING, COMPLETED, CANCELLED)
    val LABELS = mapOf(
        ALL to "All Status",
        ACTIVE to "Active",
        PAYMENT_PENDING to "Pmt Pending",
        COMPLETED to "Completed",
        CANCELLED to "Cancelled"
    )
}

object EnrollmentBillingFilter {
    const val ALL = "All"
    const val INSTALLMENT = "Installment"
    const val SUBSCRIPTION = "Subscription"

    val OPTIONS = listOf(ALL, INSTALLMENT, SUBSCRIPTION)
    val LABELS = mapOf(
        ALL to "All Billing",
        INSTALLMENT to "Installment",
        SUBSCRIPTION to "Subscription"
    )
}

enum class EnrollmentSort(val key: String, val label: String) {
    START_DATE_NEWEST("StartDateNewest", "Start date (newest)"),
    START_DATE_OLDEST("StartDateOldest", "Start date (oldest)"),
    STUDENT_NAME_ASC("StudentNameAsc", "Student name (A–Z)"),
    STUDENT_NAME_DESC("StudentNameDesc", "Student name (Z–A)"),
    BALANCE_HIGH("BalanceHigh", "Balance (high to low)");

    companion object {
        val DEFAULT = START_DATE_NEWEST
        val OPTIONS = entries.map { it.key }
        val LABELS = entries.associate { it.key to it.label }
        fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

data class EnrollmentListFiltersState(
    val search: String = "",
    val displayStatus: String = EnrollmentDisplayStatus.ALL,
    val billingType: String = EnrollmentBillingFilter.ALL,
    val courseId: String = "All",
    val sort: String = EnrollmentSort.DEFAULT.key
) {
    val hasActiveFilters: Boolean
        get() = displayStatus != EnrollmentDisplayStatus.ALL ||
            billingType != EnrollmentBillingFilter.ALL ||
            courseId != "All"

    val hasActiveSort: Boolean
        get() = sort != EnrollmentSort.DEFAULT.key
}

fun enrollmentDisplayStatusKey(enrollment: Enrollment): String = when {
    enrollment.enrollmentStatus == EnrollmentStatus.Completed -> EnrollmentDisplayStatus.COMPLETED
    enrollment.enrollmentStatus == EnrollmentStatus.Cancelled -> EnrollmentDisplayStatus.CANCELLED
    enrollment.balance > 0 -> EnrollmentDisplayStatus.PAYMENT_PENDING
    else -> EnrollmentDisplayStatus.ACTIVE
}

fun matchesEnrollmentDisplayStatus(enrollment: Enrollment, filter: String): Boolean {
    if (filter == EnrollmentDisplayStatus.ALL) return true
    return enrollmentDisplayStatusKey(enrollment) == filter
}

fun matchesEnrollmentBillingType(enrollment: Enrollment, filter: String): Boolean = when (filter) {
    EnrollmentBillingFilter.ALL -> true
    EnrollmentBillingFilter.INSTALLMENT ->
        enrollment.billingType != BillingType.Subscription
    EnrollmentBillingFilter.SUBSCRIPTION ->
        enrollment.billingType == BillingType.Subscription
    else -> true
}

fun apiStatusForDisplayFilter(displayStatus: String): String = when (displayStatus) {
    EnrollmentDisplayStatus.COMPLETED -> EnrollmentStatus.Completed.name
    EnrollmentDisplayStatus.CANCELLED -> EnrollmentStatus.Cancelled.name
    EnrollmentDisplayStatus.ACTIVE, EnrollmentDisplayStatus.PAYMENT_PENDING ->
        EnrollmentStatus.Ongoing.name
    else -> ""
}

fun applyEnrollmentFiltersAndSort(
    items: List<EnrollmentListItem>,
    filters: EnrollmentListFiltersState
): List<EnrollmentListItem> {
    val query = filters.search.trim().lowercase()
    val filtered = items.filter { item ->
        val enrollment = item.enrollment
        matchesEnrollmentDisplayStatus(enrollment, filters.displayStatus) &&
            matchesEnrollmentBillingType(enrollment, filters.billingType) &&
            (query.isBlank() ||
                item.studentName.lowercase().contains(query) ||
                item.courseName.lowercase().contains(query))
    }
    return sortEnrollmentItems(filtered, EnrollmentSort.fromKey(filters.sort))
}

private fun sortEnrollmentItems(
    items: List<EnrollmentListItem>,
    sort: EnrollmentSort
): List<EnrollmentListItem> = when (sort) {
    EnrollmentSort.START_DATE_NEWEST ->
        items.sortedByDescending { it.enrollment.startDate }
    EnrollmentSort.START_DATE_OLDEST ->
        items.sortedBy { it.enrollment.startDate }
    EnrollmentSort.STUDENT_NAME_ASC ->
        items.sortedBy { it.studentName.lowercase() }
    EnrollmentSort.STUDENT_NAME_DESC ->
        items.sortedByDescending { it.studentName.lowercase() }
    EnrollmentSort.BALANCE_HIGH ->
        items.sortedByDescending { it.enrollment.balance }
}

fun Enrollment.displayCourseName(coursesById: Map<String, Course>): String =
    courseName ?: coursesById[courseId]?.courseName ?: courseId
