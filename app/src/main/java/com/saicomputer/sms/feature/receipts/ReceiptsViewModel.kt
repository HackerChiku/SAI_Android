package com.saicomputer.sms.feature.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.ReceiptListFilters
import com.saicomputer.sms.data.model.ReceiptDetail
import com.saicomputer.sms.data.model.ReceiptListItem
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
class ReceiptsViewModel @Inject constructor(
    private val repository: ReceiptsRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(ReceiptListFiltersState())
    val filters: StateFlow<ReceiptListFiltersState> = _filters.asStateFlow()

    private val _displayItems = MutableStateFlow<UiState<List<ReceiptListItem>>>(UiState.Loading)
    val displayItems: StateFlow<UiState<List<ReceiptListItem>>> = _displayItems.asStateFlow()

    private var rawItems: List<ReceiptListItem> = emptyList()
    private var searchJob: Job? = null

    init { load() }

    fun load() {
        _displayItems.value = UiState.Loading
        viewModelScope.launch {
            try {
                rawItems = repository.list(buildApiFilters(_filters.value)).receipts
                publishDisplayItems()
            } catch (e: ApiException) {
                _displayItems.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _displayItems.value = UiState.Error(e.message ?: "Failed")
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

    fun onEmailStatusChange(value: String) {
        _filters.update { it.copy(emailStatus = value) }
        load()
    }

    fun onVoidedStatusChange(value: String) {
        _filters.update { it.copy(voidedStatus = value) }
        publishDisplayItems()
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
                emailStatus = ReceiptEmailStatusFilter.ALL,
                voidedStatus = ReceiptVoidedFilter.ALL,
                fromDate = null,
                toDate = null
            )
        }
        load()
    }

    fun clearFilters() {
        searchJob?.cancel()
        _filters.value = ReceiptListFiltersState()
        load()
    }

    val hasActiveClientFilters: Boolean
        get() {
            val f = _filters.value
            return f.search.isNotBlank() ||
                f.emailStatus != ReceiptEmailStatusFilter.ALL ||
                f.voidedStatus != ReceiptVoidedFilter.ALL ||
                !f.fromDate.isNullOrBlank() ||
                !f.toDate.isNullOrBlank() ||
                f.sort != ReceiptSort.DEFAULT.key
        }

    private fun publishDisplayItems() {
        if (_displayItems.value is UiState.Error) return
        _displayItems.value = UiState.Success(applyReceiptFiltersAndSort(rawItems, _filters.value))
    }

    private fun buildApiFilters(state: ReceiptListFiltersState): ReceiptListFilters =
        ReceiptListFilters(
            emailStatus = if (state.emailStatus == ReceiptEmailStatusFilter.ALL) null else state.emailStatus,
            dateFrom = state.fromDate,
            dateTo = state.toDate,
            pageSize = 500
        )

    fun loadReceipt(
        receiptId: String,
        onSuccess: (ReceiptDetail) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(repository.get(receiptId).receipt)
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load receipt")
            }
        }
    }

    fun resend(receiptId: String, email: String, onMessage: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.resendEmail(receiptId, email)
                onMessage("Email queued")
                load()
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            }
        }
    }
}
