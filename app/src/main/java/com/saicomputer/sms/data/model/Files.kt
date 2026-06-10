package com.saicomputer.sms.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FileBase64Response(
    val fileId: String? = null,
    val base64: String? = null,
    val mimeType: String? = null,
    val size: Int = 0
)

enum class StudentDocumentType {
    Photo,
    Aadhaar
}

enum class EmailStatus { NotSent, Queued, Sent, Failed, NotApplicable }

@Serializable
data class ReceiptListItem(
    val receiptId: String,
    val paymentId: String,
    val studentId: String,
    val studentName: String,
    val studentEmail: String = "",
    val enrollmentId: String,
    val amount: Int,
    val generatedAt: String,
    val pdfDriveId: String? = null,
    val emailStatus: EmailStatus = EmailStatus.NotSent,
    val emailedAt: String? = null,
    val voidedWithPayment: Boolean = false
)

@Serializable
data class ReceiptDetail(
    val receiptId: String,
    val paymentId: String,
    val studentId: String,
    val studentName: String,
    val studentEmail: String = "",
    val enrollmentId: String,
    val amount: Int,
    val generatedAt: String,
    val pdfDriveId: String? = null,
    val emailStatus: EmailStatus = EmailStatus.NotSent,
    val emailedAt: String? = null,
    val voidedWithPayment: Boolean = false,
    val previewUrl: String? = null,
    val downloadUrl: String? = null
)

@Serializable
data class CertificateListItem(
    val certificateId: String,
    val enrollmentId: String,
    val studentId: String,
    val studentName: String,
    val courseId: String,
    val courseName: String,
    val issueDate: String,
    val pdfDriveId: String? = null,
    val emailStatus: EmailStatus = EmailStatus.NotSent,
    val emailedAt: String? = null
)

@Serializable
data class CertificateDetail(
    val certificateId: String,
    val enrollmentId: String,
    val studentId: String,
    val studentName: String,
    val courseId: String,
    val courseName: String,
    val issueDate: String,
    val pdfDriveId: String? = null,
    val emailStatus: EmailStatus = EmailStatus.NotSent,
    val emailedAt: String? = null,
    val previewUrl: String? = null,
    val downloadUrl: String? = null
)

@Serializable
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
)
