package com.saicomputer.sms.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.model.DashboardPendingStudent
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardSummaryResponse
import com.saicomputer.sms.data.repo.DashboardRepository
import com.saicomputer.sms.data.repo.HomeDataLoader
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

data class DashboardUiState(
    val period: DashboardPeriod = DashboardPeriod.thisMonth,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val summary: DashboardSummaryResponse? = null,
    val pendingStudents: List<DashboardPendingStudent> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val homeDataLoader: HomeDataLoader,
    private val session: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state: MutableStateFlow<DashboardUiState>

    val state: StateFlow<DashboardUiState>
        get() = _state.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        val saved = savedStateHandle.get<String>(KEY_PERIOD)
        val period = saved?.let { runCatching { DashboardPeriod.valueOf(it) }.getOrNull() }
            ?: DashboardPeriod.thisMonth
        _state = MutableStateFlow(DashboardUiState(period = period))
        loadInitial(period)
    }

    fun setPeriod(period: DashboardPeriod) {
        if (period == _state.value.period && _state.value.summary != null) return
        savedStateHandle[KEY_PERIOD] = period.name
        _state.update { it.copy(period = period) }
        val cached = repository.getCachedSummary(period)
        if (cached != null) {
            applyCached(period)
            if (!repository.isCacheFresh()) {
                refreshPeriodSilently(period)
            }
        } else {
            loadPeriod(period, initial = true)
        }
    }

    fun retry() = loadPeriod(_state.value.period, initial = _state.value.summary == null)

    fun manualRefresh() {
        val user = session.currentUser.value ?: return
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            homeDataLoader.load(user.role, _state.value.period).fold(
                onSuccess = { applyCached(_state.value.period, refreshing = false) },
                onFailure = { error ->
                    _state.update { it.copy(refreshing = false) }
                    _refreshError.emit(friendlyMessage(error))
                }
            )
        }
    }

    private fun loadInitial(period: DashboardPeriod) {
        val cached = repository.getCachedSummary(period)
        if (cached != null) {
            applyCached(period)
            if (!repository.isCacheFresh()) {
                refreshPeriodSilently(period)
            }
        } else {
            loadPeriod(period, initial = true)
        }
    }

    private fun applyCached(period: DashboardPeriod, refreshing: Boolean = false) {
        _state.update {
            it.copy(
                loading = false,
                refreshing = refreshing,
                summary = repository.getCachedSummary(period),
                pendingStudents = repository.getCachedPendingStudents() ?: emptyList(),
                error = null
            )
        }
    }

    private fun refreshPeriodSilently(period: DashboardPeriod) {
        viewModelScope.launch {
            try {
                repository.refreshSummary(period)
                repository.refreshPendingStudents()
                if (_state.value.period == period) {
                    applyCached(period)
                }
            } catch (_: Exception) {
                // Keep showing cached data on background failure.
            }
        }
    }

    private fun loadPeriod(period: DashboardPeriod, initial: Boolean) {
        _state.update {
            it.copy(
                loading = initial,
                refreshing = !initial,
                error = null
            )
        }
        viewModelScope.launch {
            try {
                val summary = repository.refreshSummary(period)
                val pending = runCatching { repository.refreshPendingStudents() }
                    .getOrNull() ?: emptyList()
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        summary = summary,
                        pendingStudents = pending,
                        error = null
                    )
                }
            } catch (e: ApiException) {
                _state.update {
                    it.copy(loading = false, refreshing = false, error = e.friendlyMessage())
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = e.message ?: "Failed to load"
                    )
                }
            }
        }
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }

    companion object {
        private const val KEY_PERIOD = "dashboard_period"
    }
}
