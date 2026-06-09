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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    private var rawItems: List<ReceiptListItem> = emptyList()
    private var searchJob: Job? = null

    init {
        loadInitial()
    }

    private fun loadInitial() {
        if (isDefaultServerFilters() && tryUseCache()) {
            if (!repository.isBaseListFresh()) refreshSilently()
        } else {
            load(force = true)
        }
    }

    fun load(force: Boolean = true) {
        if (force) _displayItems.value = UiState.Loading
        viewModelScope.launch {
            try {
                rawItems = fetchItems()
                publishDisplayItems()
            } catch (e: ApiException) {
                _displayItems.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _displayItems.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun manualRefresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                rawItems = fetchItems()
                publishDisplayItems()
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun refreshSilently() {
        viewModelScope.launch {
            runCatching { fetchItems() }
                .onSuccess { items ->
                    rawItems = items
                    publishDisplayItems()
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
        if (isDefaultServerFilters() && tryUseCache()) return
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

    private fun isDefaultServerFilters(): Boolean {
        val f = _filters.value
        return f.emailStatus == ReceiptEmailStatusFilter.ALL &&
            f.fromDate.isNullOrBlank() &&
            f.toDate.isNullOrBlank()
    }

    private fun tryUseCache(): Boolean {
        val cached = repository.getCachedBaseList() ?: return false
        rawItems = cached
        publishDisplayItems()
        return true
    }

    private suspend fun fetchItems(): List<ReceiptListItem> {
        val items = if (isDefaultServerFilters()) {
            repository.refreshBaseList()
        } else {
            repository.list(buildApiFilters(_filters.value)).receipts
        }
        if (isDefaultServerFilters()) {
            repository.cacheBaseList(items)
        }
        return items
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
                load(force = false)
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            }
        }
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }
}
