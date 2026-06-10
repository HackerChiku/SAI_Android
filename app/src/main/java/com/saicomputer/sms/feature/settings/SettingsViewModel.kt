package com.saicomputer.sms.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.dto.SettingUpdateRow
import com.saicomputer.sms.data.dto.SettingsUpdateInput
import com.saicomputer.sms.data.model.SettingEntry
import com.saicomputer.sms.data.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One editable setting row, tracking the original value to detect changes. */
data class SettingRow(
    val key: String,
    val value: String,
    val category: String? = null,
    val usedIn: String? = null,
    val original: String = value
) {
    val changed: Boolean get() = value != original
}

data class SettingsUiState(
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val rows: List<SettingRow> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        val cached = repository.getCachedSettings()
        if (cached != null) {
            applySettings(cached)
            if (!repository.isSettingsFresh()) refreshSilently()
        } else {
            load(showLoading = true)
        }
    }

    fun load(showLoading: Boolean = repository.getCachedSettings() == null) {
        if (showLoading) _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                applySettings(repository.refresh())
            } catch (e: ApiException) {
                _state.update { it.copy(loading = false, error = e.friendlyMessage()) }
            }
        }
    }

    fun manualRefresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                applySettings(repository.refresh())
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun refreshSilently() {
        viewModelScope.launch {
            runCatching { repository.refresh() }
                .onSuccess { applySettings(it) }
        }
    }

    private fun applySettings(settings: List<SettingEntry>) {
        _state.update {
            it.copy(
                loading = false,
                rows = settings.map { s ->
                    SettingRow(
                        key = s.key,
                        value = s.value,
                        category = s.category,
                        usedIn = s.usedIn
                    )
                }
            )
        }
    }

    fun updateValue(key: String, value: String) {
        _state.update { st ->
            st.copy(rows = st.rows.map { if (it.key == key) it.copy(value = value) else it })
        }
    }

    fun save(onMessage: (String) -> Unit) {
        val changed = _state.value.rows.filter { it.changed }
        if (changed.isEmpty()) {
            onMessage("No changes to save")
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                repository.update(
                    SettingsUpdateInput(
                        updates = changed.map {
                            SettingUpdateRow(
                                key = it.key,
                                value = it.value,
                                category = it.category,
                                usedIn = it.usedIn
                            )
                        }
                    )
                )
                applySettings(repository.settings)
                _state.update { it.copy(submitting = false) }
                onMessage("Settings saved")
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
                onMessage(e.friendlyMessage())
            }
        }
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }
}
