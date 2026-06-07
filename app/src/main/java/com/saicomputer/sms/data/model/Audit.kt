package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AuditLogEntry(
    @SerialName("LogID") val logId: String,
    @SerialName("Timestamp") val timestamp: String = "",
    @SerialName("UserID") val userId: String = "",
    @SerialName("Action") val action: String,
    @SerialName("Entity") val entity: String? = null,
    @SerialName("EntityID") val entityId: String? = null,
    @SerialName("OldValues") val oldValues: JsonElement? = null,
    @SerialName("NewValues") val newValues: JsonElement? = null,
    @SerialName("IPAddress") val ipAddress: String? = null,
    @SerialName("UserAgent") val userAgent: String? = null
) {
    /** Short human-readable target descriptor for list rows. */
    val target: String?
        get() = when {
            entity == null -> null
            entityId == null -> entity
            else -> "$entity • $entityId"
        }
}
