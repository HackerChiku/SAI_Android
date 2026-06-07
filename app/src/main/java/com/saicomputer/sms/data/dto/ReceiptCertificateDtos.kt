package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.CertificateDetail
import com.saicomputer.sms.data.model.CertificateListItem
import com.saicomputer.sms.data.model.Pagination
import com.saicomputer.sms.data.model.ReceiptDetail
import com.saicomputer.sms.data.model.ReceiptListItem
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptListFilters(
    val studentId: String? = null,
    val enrollmentId: String? = null,
    val emailStatus: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val page: Int? = 1,
    val pageSize: Int? = 25
)

@Serializable
data class ReceiptListResponse(
    val receipts: List<ReceiptListItem> = emptyList(),
    val pagination: Pagination? = null
)

@Serializable
data class ReceiptGetResponse(val receipt: ReceiptDetail)

@Serializable
data class ResendEmailInput(
    val receiptId: String? = null,
    val certificateId: String? = null,
    val recipientEmail: String? = null
)

@Serializable
data class CertificateListFilters(
    val studentId: String? = null,
    val courseId: String? = null,
    val emailStatus: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val page: Int? = 1,
    val pageSize: Int? = 25
)

@Serializable
data class CertificateListResponse(
    val certificates: List<CertificateListItem> = emptyList(),
    val pagination: Pagination? = null
)

@Serializable
data class CertificateGetResponse(val certificate: CertificateDetail)
