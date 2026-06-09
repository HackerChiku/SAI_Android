package com.saicomputer.sms.feature.receipts

import com.saicomputer.sms.data.model.EmailStatus
import com.saicomputer.sms.data.model.ReceiptListItem

object ReceiptEmailStatusFilter {
    const val ALL = "All"

    val OPTIONS = listOf(ALL) + EmailStatus.entries.map { it.name }
    val LABELS = mapOf(
        ALL to "All Email Status",
        EmailStatus.NotSent.name to "Not Sent",
        EmailStatus.Queued.name to "Queued",
        EmailStatus.Sent.name to "Sent",
        EmailStatus.Failed.name to "Failed",
        EmailStatus.NotApplicable.name to "N/A"
    )
}

object ReceiptVoidedFilter {
    const val ALL = "All"
    const val ACTIVE = "Active"
    const val VOIDED = "Voided"

    val OPTIONS = listOf(ALL, ACTIVE, VOIDED)
    val LABELS = mapOf(
        ALL to "All Receipts",
        ACTIVE to "Active",
        VOIDED to "Voided"
    )
}

enum class ReceiptSort(val key: String, val label: String) {
    DATE_NEWEST("DateNewest", "Generated date (newest)"),
    DATE_OLDEST("DateOldest", "Generated date (oldest)"),
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

data class ReceiptListFiltersState(
    val search: String = "",
    val emailStatus: String = ReceiptEmailStatusFilter.ALL,
    val voidedStatus: String = ReceiptVoidedFilter.ALL,
    val fromDate: String? = null,
    val toDate: String? = null,
    val sort: String = ReceiptSort.DEFAULT.key
) {
    val hasActiveFilters: Boolean
        get() = emailStatus != ReceiptEmailStatusFilter.ALL ||
            voidedStatus != ReceiptVoidedFilter.ALL ||
            !fromDate.isNullOrBlank() ||
            !toDate.isNullOrBlank()

    val hasActiveSort: Boolean
        get() = sort != ReceiptSort.DEFAULT.key
}

fun applyReceiptFiltersAndSort(
    items: List<ReceiptListItem>,
    filters: ReceiptListFiltersState
): List<ReceiptListItem> {
    val query = filters.search.trim().lowercase()
    val filtered = items.filter { item ->
        matchesReceiptVoidedFilter(item, filters.voidedStatus) &&
            (query.isBlank() ||
                item.studentName.lowercase().contains(query) ||
                item.receiptId.lowercase().contains(query))
    }
    return sortReceiptItems(filtered, ReceiptSort.fromKey(filters.sort))
}

private fun matchesReceiptVoidedFilter(item: ReceiptListItem, filter: String): Boolean = when (filter) {
    ReceiptVoidedFilter.ACTIVE -> !item.voidedWithPayment
    ReceiptVoidedFilter.VOIDED -> item.voidedWithPayment
    else -> true
}

private fun sortReceiptItems(
    items: List<ReceiptListItem>,
    sort: ReceiptSort
): List<ReceiptListItem> = when (sort) {
    ReceiptSort.DATE_NEWEST ->
        items.sortedByDescending { it.generatedAt }
    ReceiptSort.DATE_OLDEST ->
        items.sortedBy { it.generatedAt }
    ReceiptSort.AMOUNT_HIGH ->
        items.sortedByDescending { it.amount }
    ReceiptSort.AMOUNT_LOW ->
        items.sortedBy { it.amount }
    ReceiptSort.STUDENT_NAME_ASC ->
        items.sortedBy { it.studentName.lowercase() }
}
