package com.saicomputer.sms.feature.certificates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.CertificateListFilters
import com.saicomputer.sms.data.model.CertificateListItem
import com.saicomputer.sms.data.repo.CertificatesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val repository: CertificatesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<CertificateListItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CertificateListItem>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.list(CertificateListFilters()).certificates)
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun resend(certificateId: String, email: String, onMessage: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.resendEmail(certificateId, email)
                onMessage("Email queued")
                load()
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            }
        }
    }
}
