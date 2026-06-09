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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuditUiFilters(
    val action: String = "All",
    val date: String? = null
)

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val repository: AuditRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<AuditLogEntry>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AuditLogEntry>>> = _state.asStateFlow()

    private val _filters = MutableStateFlow(AuditUiFilters())
    val filters: StateFlow<AuditUiFilters> = _filters.asStateFlow()

    private val _actionOptions = MutableStateFlow(listOf("All"))
    val actionOptions: StateFlow<List<String>> = _actionOptions.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val f = _filters.value
                val rows = repository.list(
                    AuditListFilters(
                        action = f.action.takeIf { it != "All" },
                        from = f.date,
                        to = f.date
                    )
                ).rows
                _actionOptions.update { current ->
                    (current + rows.map { it.action }).distinct().sorted().let { actions ->
                        listOf("All") + actions.filter { it != "All" }
                    }
                }
                _state.value = UiState.Success(rows)
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun onActionChange(action: String) {
        _filters.update { it.copy(action = action) }
        load()
    }

    fun onDateChange(date: String?) {
        _filters.update { it.copy(date = date) }
        load()
    }

    fun clearDateFilter() {
        _filters.update { it.copy(date = null) }
        load()
    }
}
