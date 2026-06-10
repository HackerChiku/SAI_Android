package com.saicomputer.sms.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.CoursesRepository
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
class CoursesListViewModel @Inject constructor(
    private val repository: CoursesRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Course>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Course>>> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    init {
        loadInitial()
    }

    private fun loadInitial() {
        val cached = repository.getCachedList()
        if (cached != null) {
            _state.value = UiState.Success(cached)
            if (!repository.isListFresh()) refreshSilently()
        } else {
            load(force = true)
        }
    }

    fun load(force: Boolean = true) {
        if (force) _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshList())
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun manualRefresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshList())
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun refreshSilently() {
        viewModelScope.launch {
            runCatching { repository.refreshList() }
                .onSuccess { if (_state.value !is UiState.Error) _state.value = UiState.Success(it) }
        }
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }
}
