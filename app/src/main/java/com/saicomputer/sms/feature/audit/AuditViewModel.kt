package com.saicomputer.sms.feature.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.AuditListFilters
import com.saicomputer.sms.data.model.AuditLogEntry
import com.saicomputer.sms.data.repo.AuditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<AuditLogEntry>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AuditLogEntry>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.list(AuditListFilters()).rows)
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }
}
