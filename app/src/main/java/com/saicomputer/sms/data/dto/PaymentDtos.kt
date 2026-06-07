package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.PaymentMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface PaymentCreateInput {
    val isBackdate: Boolean?
    val effectiveCreatedAt: String?
}

@Serializable
data class InstallmentPaymentCreateInput(
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("InstallmentID") val installmentId: String,
    @SerialName("Amount") val amount: Int,
    @SerialName("PaymentMethod") val paymentMethod: PaymentMethod,
    @SerialName("UPITransactionRef") val upiTransactionRef: String? = null,
    @SerialName("PaymentDate") val paymentDate: String,
    @SerialName("Notes") val notes: String? = null,
    override val isBackdate: Boolean? = null,
    override val effectiveCreatedAt: String? = null
) : PaymentCreateInput

@Serializable
data class SubscriptionPaymentCreateInput(
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("Amount") val amount: Int,
    @SerialName("PaymentDate") val paymentDate: String,
    @SerialName("PaymentMethod") val paymentMethod: PaymentMethod,
    val billingMonth: String,
    val usePackage: Boolean = false,
    @SerialName("Notes") val notes: String? = null,
    override val isBackdate: Boolean? = null,
    override val effectiveCreatedAt: String? = null
) : PaymentCreateInput

@Serializable
data class PaymentCreateResponse(
    @SerialName("PaymentID") val paymentId: String? = null,
    @SerialName("ReceiptID") val receiptId: String? = null,
    val installmentStatus: String? = null,
    val installmentBalance: Int? = null,
    val enrollmentBalance: Int? = null,
    val isEnrollmentFullyPaid: Boolean? = null,
    val billingType: String? = null,
    val billingMonth: String? = null,
    val paymentType: String? = null,
    val totalAmount: Int? = null
)

@Serializable
data class VoidPaymentInput(
    @SerialName("PaymentID") val paymentId: String,
    @SerialName("VoidReason") val reason: String
)

@Serializable
data class EditBillingMonthInput(
    val paymentId: String,
    val newBillingMonth: String
)

@Serializable
data class PaymentListResponse(
    val total: Int = 0,
    val offset: Int = 0,
    val limit: Int = 50,
    val rows: List<Payment> = emptyList()
)
