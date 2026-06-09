package com.saicomputer.sms.feature.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.EditBillingMonthInput
import com.saicomputer.sms.data.dto.PaymentListFilters
import com.saicomputer.sms.data.dto.VoidPaymentInput
import com.saicomputer.sms.data.model.PaymentListItem
import com.saicomputer.sms.data.model.ReceiptDetail
import com.saicomputer.sms.data.repo.PaymentsRepository
import com.saicomputer.sms.data.repo.ReceiptsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentsListViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val receiptsRepository: ReceiptsRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(PaymentListFiltersState())
    val filters: StateFlow<PaymentListFiltersState> = _filters.asStateFlow()

    private val _displayItems = MutableStateFlow<UiState<List<PaymentListItem>>>(UiState.Loading)
    val displayItems: StateFlow<UiState<List<PaymentListItem>>> = _displayItems.asStateFlow()

    private var rawItems: List<PaymentListItem> = emptyList()
    private var searchJob: Job? = null

    init { load() }

    fun load() {
        _displayItems.value = UiState.Loading
        viewModelScope.launch {
            try {
                rawItems = paymentsRepository.list(buildApiFilters(_filters.value))
                publishDisplayItems()
            } catch (e: ApiException) {
                _displayItems.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _displayItems.value = UiState.Error(e.message ?: "Failed to load payments")
            }
        }
    }

    fun onSearchChange(value: String) {
        _filters.update { it.copy(search = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(if (value.isBlank()) 0L else 300L)
            publishDisplayItems()
        }
    }

    fun onStatusChange(value: String) {
        _filters.update { it.copy(status = value) }
        load()
    }

    fun onPaymentMethodChange(value: String) {
        _filters.update { it.copy(paymentMethod = value) }
        load()
    }

    fun onFromDateChange(value: String?) {
        _filters.update { it.copy(fromDate = value) }
        load()
    }

    fun onToDateChange(value: String?) {
        _filters.update { it.copy(toDate = value) }
        load()
    }

    fun onSortChange(value: String) {
        _filters.update { it.copy(sort = value) }
        publishDisplayItems()
    }

    fun clearFilterFields() {
        _filters.update {
            it.copy(
                status = PaymentStatusFilter.ALL,
                paymentMethod = PaymentMethodFilter.ALL,
                fromDate = null,
                toDate = null
            )
        }
        load()
    }

    fun clearFilters() {
        searchJob?.cancel()
        _filters.value = PaymentListFiltersState()
        load()
    }

    val hasActiveClientFilters: Boolean
        get() {
            val f = _filters.value
            return f.search.isNotBlank() ||
                f.status != PaymentStatusFilter.ALL ||
                f.paymentMethod != PaymentMethodFilter.ALL ||
                !f.fromDate.isNullOrBlank() ||
                !f.toDate.isNullOrBlank() ||
                f.sort != PaymentSort.DEFAULT.key
        }

    private fun publishDisplayItems() {
        if (_displayItems.value is UiState.Error) return
        _displayItems.value = UiState.Success(applyPaymentFiltersAndSort(rawItems, _filters.value))
    }

    private fun buildApiFilters(state: PaymentListFiltersState): PaymentListFilters =
        PaymentListFilters(
            status = if (state.status == PaymentStatusFilter.ALL) "" else state.status,
            paymentMethod = if (state.paymentMethod == PaymentMethodFilter.ALL) "" else state.paymentMethod,
            from = state.fromDate.orEmpty(),
            to = state.toDate.orEmpty(),
            limit = 500
        )

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
