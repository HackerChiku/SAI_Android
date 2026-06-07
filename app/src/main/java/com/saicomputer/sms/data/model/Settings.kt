package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single institute setting (key-value), as returned by settings.getAll. */
@Serializable
data class SettingEntry(
    @SerialName("Key") val key: String,
    @SerialName("Value") val value: String = "",
    @SerialName("Category") val category: String? = null,
    @SerialName("UsedIn") val usedIn: String? = null,
    @SerialName("UpdatedAt") val updatedAt: String? = null,
    @SerialName("UpdatedBy") val updatedBy: String? = null
)
