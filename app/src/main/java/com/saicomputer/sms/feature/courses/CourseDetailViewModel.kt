package com.saicomputer.sms.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.CourseTopic
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.CoursesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseDetailData(
    val course: Course,
    val topics: List<CourseTopic> = emptyList()
)

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val repository: CoursesRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CourseDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<CourseDetailData>> = _state.asStateFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    private var courseId: String = ""

    fun load(id: String) {
        courseId = id
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val course = repository.get(id).course
                val topics = if (course.hasTopics) {
                    runCatching { repository.listTopics(id).topics }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }
                _state.value = UiState.Success(CourseDetailData(course, topics))
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load course")
            }
        }
    }

    fun reload() {
        if (courseId.isNotBlank()) load(courseId)
    }
}
