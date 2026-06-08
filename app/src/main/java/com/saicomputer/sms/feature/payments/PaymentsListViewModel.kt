package com.saicomputer.sms.feature.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.EditBillingMonthInput
import com.saicomputer.sms.data.dto.VoidPaymentInput
import com.saicomputer.sms.data.model.PaymentListItem
import com.saicomputer.sms.data.model.ReceiptDetail
import com.saicomputer.sms.data.repo.PaymentsRepository
import com.saicomputer.sms.data.repo.ReceiptsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentsListViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val receiptsRepository: ReceiptsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<PaymentListItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<PaymentListItem>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(paymentsRepository.list())
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load payments")
            }
        }
    }

    fun voidPayment(
        paymentId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                paymentsRepository.void(VoidPaymentInput(paymentId, reason))
                onSuccess()
                load()
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to void payment")
            }
        }
    }

    fun editBillingMonth(
        paymentId: String,
        newBillingMonth: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                paymentsRepository.editBillingMonth(
                    EditBillingMonthInput(paymentId, newBillingMonth)
                )
                onSuccess()
                load()
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update billing month")
            }
        }
    }

    fun loadReceipt(
        receiptId: String,
        onSuccess: (ReceiptDetail) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(receiptsRepository.get(receiptId).receipt)
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load receipt")
            }
        }
    }
}
