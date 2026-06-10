package com.saicomputer.sms.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.CourseDetailEntry
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
class CourseDetailViewModel @Inject constructor(
    private val repository: CoursesRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CourseDetailEntry>>(UiState.Loading)
    val state: StateFlow<UiState<CourseDetailEntry>> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    private var courseId: String = ""

    fun load(id: String) {
        courseId = id
        val cached = repository.getCachedCourse(id)
        if (cached != null) {
            _state.value = UiState.Success(cached)
            if (!repository.isCourseFresh(id)) refreshSilently(id)
        } else {
            fetch(id)
        }
    }

    private fun fetch(id: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshCourse(id))
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage(), e.code)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load course")
            }
        }
    }

    private fun refreshSilently(id: String) {
        viewModelScope.launch {
            runCatching { repository.refreshCourse(id) }
                .onSuccess { if (courseId == id) _state.value = UiState.Success(it) }
        }
    }

    fun manualRefresh() {
        val id = courseId
        if (id.isBlank() || _refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshCourse(id))
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun reload() = fetch(courseId)

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }
}
