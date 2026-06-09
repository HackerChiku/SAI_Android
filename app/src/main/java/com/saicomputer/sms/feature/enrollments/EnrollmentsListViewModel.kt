package com.saicomputer.sms.feature.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.EnrollmentListFilters
import com.saicomputer.sms.data.dto.StudentListFilters
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.repo.CoursesRepository
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnrollmentListItem(
    val enrollment: Enrollment,
    val studentName: String,
    val courseName: String
)

data class CourseFilterOption(
    val courseId: String,
    val label: String
)

@HiltViewModel
class EnrollmentsListViewModel @Inject constructor(
    private val repository: EnrollmentsRepository,
    private val studentsRepository: StudentsRepository,
    private val coursesRepository: CoursesRepository
) : ViewModel() {

    private val _filters = MutableStateFlow(EnrollmentListFiltersState())
    val filters: StateFlow<EnrollmentListFiltersState> = _filters.asStateFlow()

    private val _courses = MutableStateFlow<List<CourseFilterOption>>(emptyList())
    val courses: StateFlow<List<CourseFilterOption>> = _courses.asStateFlow()

    private val _displayItems = MutableStateFlow<UiState<List<EnrollmentListItem>>>(UiState.Loading)
    val displayItems: StateFlow<UiState<List<EnrollmentListItem>>> = _displayItems.asStateFlow()

    private var rawItems: List<EnrollmentListItem> = emptyList()
    private var searchJob: Job? = null

    init {
        loadCourses()
        load()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            runCatching {
                coursesRepository.list().rows.map { course ->
                    CourseFilterOption(
                        courseId = course.courseId,
                        label = course.courseName.ifBlank { course.courseFullName }
                    )
                }
            }.onSuccess { _courses.value = it }
        }
    }

    fun load() {
        _displayItems.value = UiState.Loading
        viewModelScope.launch {
            try {
                rawItems = loadItems()
                publishDisplayItems()
            } catch (e: ApiException) {
                _displayItems.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _displayItems.value = UiState.Error(e.message ?: "Failed to load enrollments")
            }
        }
    }

    fun onSearchChange(value: String) {
        _filters.update { it.copy(search = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(if (value.isBlank()) 0L else 300L)
            publishDisplayItems()
        }
    }

    fun onDisplayStatusChange(value: String) {
        _filters.update { it.copy(displayStatus = value) }
        reloadIfServerFiltersChanged()
    }

    fun onBillingTypeChange(value: String) {
        _filters.update { it.copy(billingType = value) }
        publishDisplayItems()
    }

    fun onCourseChange(value: String) {
        _filters.update { it.copy(courseId = value) }
        reloadIfServerFiltersChanged()
    }

    fun onSortChange(value: String) {
        _filters.update { it.copy(sort = value) }
        publishDisplayItems()
    }

    fun clearFilterFields() {
        _filters.update {
            it.copy(
                displayStatus = EnrollmentDisplayStatus.ALL,
                billingType = EnrollmentBillingFilter.ALL,
                courseId = "All"
            )
        }
        load()
    }

    fun clearFilters() {
        searchJob?.cancel()
        _filters.value = EnrollmentListFiltersState()
        load()
    }

    val hasActiveClientFilters: Boolean
        get() {
            val f = _filters.value
            return f.search.isNotBlank() ||
                f.displayStatus != EnrollmentDisplayStatus.ALL ||
                f.billingType != EnrollmentBillingFilter.ALL ||
                f.courseId != "All" ||
                f.sort != EnrollmentSort.DEFAULT.key
        }

    private fun reloadIfServerFiltersChanged() {
        load()
    }

    private fun publishDisplayItems() {
        if (_displayItems.value is UiState.Error) return
        _displayItems.value = UiState.Success(applyEnrollmentFiltersAndSort(rawItems, _filters.value))
    }

    private suspend fun loadItems(): List<EnrollmentListItem> = coroutineScope {
        val currentFilters = _filters.value
        val apiFilters = EnrollmentListFilters(
            courseId = if (currentFilters.courseId == "All") "" else currentFilters.courseId,
            status = apiStatusForDisplayFilter(currentFilters.displayStatus),
            limit = 500
        )

        val enrollmentsDeferred = async { repository.list(apiFilters).rows }
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
            enrollment.toListItem(studentsById, coursesById)
        }
    }

    private fun Enrollment.toListItem(
        studentsById: Map<String, com.saicomputer.sms.data.model.Student>,
        coursesById: Map<String, Course>
    ): EnrollmentListItem {
        val studentName = studentName
            ?: studentsById[studentId]?.fullName
            ?: studentId
            ?: "Unknown student"
        val courseName = courseName
            ?: coursesById[courseId]?.courseName
            ?: courseId
        return EnrollmentListItem(
            enrollment = this,
            studentName = studentName,
            courseName = courseName
        )
    }
}
