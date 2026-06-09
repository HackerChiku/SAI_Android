package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class PaymentMethod { UPI, CASH, QR, BANK_TRANSFER }
enum class PaymentStatus { Active, Voided }
enum class PaymentType { Regular, Package, Bonus, PackageBonus }

@Serializable
data class Payment(
    @SerialName("PaymentID") val paymentId: String,
    @SerialName("EnrollmentID") val enrollmentId: String,
    @SerialName("InstallmentID") val installmentId: String? = null,
    @SerialName("Amount") val amount: Int,
    @SerialName("PaymentMethod") val paymentMethod: PaymentMethod,
    @SerialName("UPITransactionRef") val upiTransactionRef: String? = null,
    @SerialName("PaymentDate") val paymentDate: String,
    @SerialName("Status") val status: PaymentStatus = PaymentStatus.Active,
    @SerialName("VoidedBy") val voidedBy: String? = null,
    @SerialName("VoidedAt") val voidedAt: String? = null,
    @SerialName("VoidReason") val voidReason: String? = null,
    @SerialName("ReceiptID") val receiptId: String? = null,
    @SerialName("Notes") val notes: String? = null,
    @SerialName("BillingMonth") val billingMonth: String? = null,
    @SerialName("PaymentType") val paymentType: PaymentType = PaymentType.Regular,
    @SerialName("BonusForPaymentID") val bonusForPaymentId: String? = null,
    @SerialName("CreatedAt") val createdAt: String = "",
    @SerialName("CreatedBy") val createdBy: String = ""
)

/** Display row for the global payment history list (payment + joined labels). */
data class PaymentListItem(
    val payment: Payment,
    val studentName: String,
    val courseName: String = "",
    val registrationSession: RegistrationSession? = null
)
