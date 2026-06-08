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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val repository: ReceiptsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ReceiptListItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ReceiptListItem>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.list(ReceiptListFilters()).receipts)
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
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
