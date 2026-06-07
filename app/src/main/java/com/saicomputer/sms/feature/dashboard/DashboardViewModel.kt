package com.saicomputer.sms.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.model.DashboardPendingStudent
import com.saicomputer.sms.data.model.DashboardPeriod
import com.saicomputer.sms.data.model.DashboardSummaryResponse
import com.saicomputer.sms.data.repo.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state: MutableStateFlow<DashboardUiState>

    val state: StateFlow<DashboardUiState>
        get() = _state.asStateFlow()

    init {
        val saved = savedStateHandle.get<String>(KEY_PERIOD)
        val period = saved?.let { runCatching { DashboardPeriod.valueOf(it) }.getOrNull() }
            ?: DashboardPeriod.thisMonth
        _state = MutableStateFlow(DashboardUiState(period = period))
        load(period, initial = true)
    }

    fun setPeriod(period: DashboardPeriod) {
        if (period == _state.value.period && _state.value.summary != null) return
        savedStateHandle[KEY_PERIOD] = period.name
        _state.update { it.copy(period = period) }
        load(period, initial = false)
    }

    fun retry() = load(_state.value.period, initial = _state.value.summary == null)

    private fun load(period: DashboardPeriod, initial: Boolean) {
        _state.update {
            it.copy(
                loading = initial,
                refreshing = !initial,
                error = null
            )
        }
        viewModelScope.launch {
            try {
                val summary = repository.summary(period)
                val pending = runCatching { repository.paymentPendingStudents() }
                    .getOrNull()?.students ?: emptyList()
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
                    it.copy(loading = false, refreshing = false, error = e.message ?: "Failed to load")
                }
            }
        }
    }

    companion object {
        private const val KEY_PERIOD = "dashboard_period"
    }
}
