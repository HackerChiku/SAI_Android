package com.saicomputer.sms.feature.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.EnrollmentListFilters
import com.saicomputer.sms.data.dto.StudentListFilters
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.repo.CoursesRepository
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnrollmentListItem(
    val enrollment: Enrollment,
    val studentName: String,
    val courseName: String
)

@HiltViewModel
class EnrollmentsListViewModel @Inject constructor(
    private val repository: EnrollmentsRepository,
    private val studentsRepository: StudentsRepository,
    private val coursesRepository: CoursesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<EnrollmentListItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<EnrollmentListItem>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(loadItems())
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load enrollments")
            }
        }
    }

    private suspend fun loadItems(): List<EnrollmentListItem> = coroutineScope {
        val enrollmentsDeferred = async { repository.list(EnrollmentListFilters()).rows }
        val studentsDeferred = async {
            studentsRepository.list(StudentListFilters(limit = 1000)).rows.associateBy { it.studentId }
        }
        val coursesDeferred = async {
            coursesRepository.list().rows.associateBy { it.courseId }
        }

        val enrollments = enrollmentsDeferred.await()
        val studentsById = studentsDeferred.await()
        val coursesById = coursesDeferred.await()

        enrollments.map { enrollment ->
            val studentName = enrollment.studentName
                ?: studentsById[enrollment.studentId]?.fullName
                ?: enrollment.studentId
                ?: "Unknown student"
            val courseName = enrollment.courseName
                ?: coursesById[enrollment.courseId]?.courseName
                ?: enrollment.courseId
            EnrollmentListItem(
                enrollment = enrollment,
                studentName = studentName,
                courseName = courseName
            )
        }
    }
}
