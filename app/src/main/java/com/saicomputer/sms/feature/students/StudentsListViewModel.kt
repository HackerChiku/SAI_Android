package com.saicomputer.sms.feature.students

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.repo.StudentsRepository
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

data class StudentFilters(
    val search: String = "",
    val status: String = "All",
    val registrationSession: String = "All"
)

@HiltViewModel
class StudentsListViewModel @Inject constructor(
    private val repository: StudentsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _filters = MutableStateFlow(
        StudentFilters(
            search = savedStateHandle.get<String>(KEY_SEARCH).orEmpty(),
            status = savedStateHandle.get<String>(KEY_STATUS) ?: "All",
            registrationSession = savedStateHandle.get<String>(KEY_SESSION) ?: "All"
        )
    )
    val filters: StateFlow<StudentFilters> = _filters.asStateFlow()

    private val _displayItems = MutableStateFlow<UiState<List<Student>>>(UiState.Loading)
    val displayItems: StateFlow<UiState<List<Student>>> = _displayItems.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadInitial()
    }

    private fun loadInitial() {
        val cached = repository.getCachedBaseList()
        if (cached != null) {
            publishFromRaw(cached)
            if (!repository.isBaseListFresh()) {
                refreshSilently()
            }
        } else {
            load(force = true)
        }
    }

    fun manualRefresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                publishFromRaw(repository.refreshBaseList())
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun load(force: Boolean = true) {
        if (force) _displayItems.value = UiState.Loading
        viewModelScope.launch {
            try {
                publishFromRaw(repository.refreshBaseList())
            } catch (e: ApiException) {
                _displayItems.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _displayItems.value = UiState.Error(e.message ?: "Failed to load students")
            }
        }
    }

    private fun refreshSilently() {
        viewModelScope.launch {
            runCatching { repository.refreshBaseList() }
                .onSuccess { publishFromRaw(it) }
        }
    }

    private fun publishFromRaw(raw: List<Student>) {
        if (_displayItems.value is UiState.Error) return
        _displayItems.value = UiState.Success(applyStudentFilters(raw, _filters.value))
    }

    fun onSearchChange(value: String) {
        savedStateHandle[KEY_SEARCH] = value
        _filters.update { it.copy(search = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(if (value.isBlank()) 0L else 300L)
            repository.getCachedBaseList()?.let { publishFromRaw(it) }
        }
    }

    fun onStatusChange(value: String) {
        savedStateHandle[KEY_STATUS] = value
        _filters.update { it.copy(status = value) }
        repository.getCachedBaseList()?.let { publishFromRaw(it) }
    }

    fun onSessionChange(value: String) {
        savedStateHandle[KEY_SESSION] = value
        _filters.update { it.copy(registrationSession = value) }
        repository.getCachedBaseList()?.let { publishFromRaw(it) }
    }

    fun clearFilters() {
        searchJob?.cancel()
        savedStateHandle[KEY_SEARCH] = ""
        savedStateHandle[KEY_STATUS] = "All"
        savedStateHandle[KEY_SESSION] = "All"
        _filters.value = StudentFilters()
        repository.getCachedBaseList()?.let { publishFromRaw(it) } ?: load(force = true)
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }

    companion object {
        private const val KEY_SEARCH = "students_search"
        private const val KEY_STATUS = "students_status"
        private const val KEY_SESSION = "students_session"
    }
}
