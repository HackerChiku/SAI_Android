package com.saicomputer.sms.feature.payments

import com.saicomputer.sms.data.model.PaymentListItem
import com.saicomputer.sms.data.model.PaymentStatus

object PaymentStatusFilter {
    const val ALL = "All"
    const val PAID = "Active"
    const val VOIDED = "Voided"

    val OPTIONS = listOf(ALL, PAID, VOIDED)
    val LABELS = mapOf(
        ALL to "All Status",
        PAID to "Paid",
        VOIDED to "Voided"
    )
}

object PaymentMethodFilter {
    const val ALL = "All"
    const val UPI = "UPI"
    const val CASH = "CASH"
    const val QR = "QR"
    const val BANK_TRANSFER = "BANK_TRANSFER"

    val OPTIONS = listOf(ALL, UPI, CASH, QR, BANK_TRANSFER)
    val LABELS = mapOf(
        ALL to "All Methods",
        UPI to "UPI",
        CASH to "Cash",
        QR to "QR",
        BANK_TRANSFER to "Bank Transfer"
    )
}

enum class PaymentSort(val key: String, val label: String) {
    DATE_NEWEST("DateNewest", "Payment date (newest)"),
    DATE_OLDEST("DateOldest", "Payment date (oldest)"),
    AMOUNT_HIGH("AmountHigh", "Amount (high to low)"),
    AMOUNT_LOW("AmountLow", "Amount (low to high)"),
    STUDENT_NAME_ASC("StudentNameAsc", "Student name (A–Z)");

    companion object {
        val DEFAULT = DATE_NEWEST
        val OPTIONS = entries.map { it.key }
        val LABELS = entries.associate { it.key to it.label }
        fun fromKey(key: String) = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

data class PaymentListFiltersState(
    val search: String = "",
    val status: String = PaymentStatusFilter.ALL,
    val paymentMethod: String = PaymentMethodFilter.ALL,
    val fromDate: String? = null,
    val toDate: String? = null,
    val sort: String = PaymentSort.DEFAULT.key
) {
    val hasActiveFilters: Boolean
        get() = status != PaymentStatusFilter.ALL ||
            paymentMethod != PaymentMethodFilter.ALL ||
            !fromDate.isNullOrBlank() ||
            !toDate.isNullOrBlank()

    val hasActiveSort: Boolean
        get() = sort != PaymentSort.DEFAULT.key
}

fun applyPaymentFiltersAndSort(
    items: List<PaymentListItem>,
    filters: PaymentListFiltersState
): List<PaymentListItem> {
    val query = filters.search.trim().lowercase()
    val filtered = items.filter { item ->
        query.isBlank() ||
            item.studentName.lowercase().contains(query) ||
            item.courseName.lowercase().contains(query)
    }
    return sortPaymentItems(filtered, PaymentSort.fromKey(filters.sort))
}

private fun sortPaymentItems(
    items: List<PaymentListItem>,
    sort: PaymentSort
): List<PaymentListItem> = when (sort) {
    PaymentSort.DATE_NEWEST ->
        items.sortedByDescending { it.payment.paymentDate }
    PaymentSort.DATE_OLDEST ->
        items.sortedBy { it.payment.paymentDate }
    PaymentSort.AMOUNT_HIGH ->
        items.sortedByDescending { it.payment.amount }
    PaymentSort.AMOUNT_LOW ->
        items.sortedBy { it.payment.amount }
    PaymentSort.STUDENT_NAME_ASC ->
        items.sortedBy { it.studentName.lowercase() }
}
