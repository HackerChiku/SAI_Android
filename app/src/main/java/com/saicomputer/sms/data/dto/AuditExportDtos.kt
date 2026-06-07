package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.AuditLogEntry
import kotlinx.serialization.Serializable

@Serializable
data class AuditListFilters(
    val from: String? = null,
    val to: String? = null,
    val userId: String? = null,
    val entity: String? = null,
    val entityId: String? = null,
    val action: String? = null,
    val limit: Int = 100,
    val offset: Int = 0
)

@Serializable
data class AuditListResponse(
    val total: Int = 0,
    val offset: Int = 0,
    val limit: Int = 100,
    val rows: List<AuditLogEntry> = emptyList()
)

@Serializable
data class ExportResponse(
    val filename: String? = null,
    val fileId: String? = null,
    val viewUrl: String? = null,
    val downloadUrl: String? = null,
    val rowCount: Int = 0
)
