package com.saicomputer.sms.feature.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.InstallmentPaymentCreateInput
import com.saicomputer.sms.data.dto.SubscriptionPaymentCreateInput
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.data.model.PaymentMethod
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import com.saicomputer.sms.data.repo.PaymentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PaymentFormState(
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val enrollment: Enrollment? = null,
    val installments: List<Installment> = emptyList(),
    val selectedInstallmentId: String? = null,
    val amount: Int = 0,
    val method: PaymentMethod = PaymentMethod.UPI,
    val upiRef: String = "",
    val paymentDate: String = LocalDate.now().toString(),
    val billingMonth: String = "",
    val usePackage: Boolean = false,
    val notes: String = "",
    val backdateEnabled: Boolean = false,
    val effectiveCreatedAt: String? = null,
    val error: String? = null
) {
    val isSubscription: Boolean get() = enrollment?.billingType == BillingType.Subscription
    val upiRefError: String?
        get() = if (method == PaymentMethod.UPI && upiRef.isBlank()) "UPI reference is required" else null
    val canSubmit: Boolean
        get() = enrollment != null &&
            amount > 0 &&
            upiRefError == null &&
            (isSubscription || selectedInstallmentId != null) &&
            (!isSubscription || billingMonth.isNotBlank()) &&
            !submitting
}

@HiltViewModel
class PaymentFormViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val enrollmentsRepository: EnrollmentsRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentFormState())
    val state: StateFlow<PaymentFormState> = _state.asStateFlow()
    val currentUser: StateFlow<User?> = session.currentUser

    private var enrollmentId: String = ""

    fun initialize(id: String) {
        if (enrollmentId == id && _state.value.enrollment != null) return
        enrollmentId = id
        _state.value = PaymentFormState(loading = true)
        viewModelScope.launch {
            try {
                val e = enrollmentsRepository.get(id).enrollment
                val installments = e.installments.orEmpty()
                    .filter { it.amountPaid < it.amountDue }
                val firstUnpaid = installments.firstOrNull()
                _state.update {
                    it.copy(
                        loading = false,
                        enrollment = e,
                        installments = installments,
                        selectedInstallmentId = firstUnpaid?.installmentId,
                        amount = firstUnpaid?.let { inst -> inst.amountDue - inst.amountPaid }
                            ?: (e.monthlyFee ?: 0),
                        billingMonth = if (e.billingType == BillingType.Subscription) LocalDate.now().toString().substring(0, 7) else ""
                    )
                }
            } catch (e: ApiException) {
                _state.update { it.copy(loading = false, error = e.friendlyMessage()) }
            }
        }
    }

    fun update(transform: (PaymentFormState) -> PaymentFormState) = _state.update(transform)

    fun selectInstallment(installment: Installment) {
        _state.update {
            it.copy(
                selectedInstallmentId = installment.installmentId,
                amount = installment.amountDue - installment.amountPaid
            )
        }
    }

    fun submit(onSuccess: () -> Unit, onMessage: (String) -> Unit) {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                if (s.isSubscription) {
                    paymentsRepository.create(
                        SubscriptionPaymentCreateInput(
                            enrollmentId = enrollmentId,
                            amount = s.amount,
                            paymentDate = s.paymentDate,
                            paymentMethod = s.method,
                            billingMonth = s.billingMonth,
                            usePackage = s.usePackage,
                            notes = s.notes.ifBlank { null },
                            isBackdate = if (s.backdateEnabled) true else null,
                            effectiveCreatedAt = if (s.backdateEnabled) s.effectiveCreatedAt else null
                        )
                    )
                } else {
                    paymentsRepository.create(
                        InstallmentPaymentCreateInput(
                            enrollmentId = enrollmentId,
                            installmentId = s.selectedInstallmentId!!,
                            amount = s.amount,
                            paymentMethod = s.method,
                            upiTransactionRef = s.upiRef.ifBlank { null },
                            paymentDate = s.paymentDate,
                            notes = s.notes.ifBlank { null },
                            isBackdate = if (s.backdateEnabled) true else null,
                            effectiveCreatedAt = if (s.backdateEnabled) s.effectiveCreatedAt else null
                        )
                    )
                }
                _state.update { it.copy(submitting = false) }
                onMessage("Payment recorded")
                onSuccess()
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
                onMessage(e.friendlyMessage())
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message) }
                onMessage(e.message ?: "Failed")
            }
        }
    }
}
