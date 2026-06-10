package com.saicomputer.sms.feature.certificates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.model.CertificateDetail
import com.saicomputer.sms.data.model.CertificateListItem
import com.saicomputer.sms.data.repo.CertificatesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val repository: CertificatesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<CertificateListItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CertificateListItem>>> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        val cached = repository.getCachedBaseList()
        if (cached != null) {
            _state.value = UiState.Success(cached)
            if (!repository.isBaseListFresh()) refreshSilently()
        } else {
            load(force = true)
        }
    }

    fun load(force: Boolean = true) {
        if (force) _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshBaseList())
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun manualRefresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshBaseList())
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun refreshSilently() {
        viewModelScope.launch {
            runCatching { repository.refreshBaseList() }
                .onSuccess { if (_state.value !is UiState.Error) _state.value = UiState.Success(it) }
        }
    }

    fun loadCertificate(
        certificateId: String,
        onSuccess: (CertificateDetail) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(repository.get(certificateId).certificate)
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load certificate")
            }
        }
    }

    fun resend(certificateId: String, email: String, onMessage: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.resendEmail(certificateId, email)
                onMessage("Email queued")
                runCatching { repository.refreshBaseList() }
                    .onSuccess { if (_state.value !is UiState.Error) _state.value = UiState.Success(it) }
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
