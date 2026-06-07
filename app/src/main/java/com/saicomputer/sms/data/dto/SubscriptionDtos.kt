package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.SubscriptionListItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionListFilters(
    val status: String? = null,
    val pendingOnly: Boolean? = null,
    val studentSearch: String? = null
)

@Serializable
data class SubscriptionListResponse(
    val rows: List<SubscriptionListItem> = emptyList(),
    val total: Int = 0
)

@Serializable
data class ExtendSubscriptionInput(
    val enrollmentId: String,
    @SerialName("Months") val months: Int
)

@Serializable
data class EditEndDateInput(val enrollmentId: String, val newEndDate: String)
