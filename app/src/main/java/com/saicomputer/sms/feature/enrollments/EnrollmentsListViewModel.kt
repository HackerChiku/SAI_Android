package com.saicomputer.sms.feature.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.EnrollmentListFilters
import com.saicomputer.sms.data.dto.StudentListFilters
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.Enrollment
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.repo.CoursesRepository
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    private var rawItems: List<EnrollmentListItem> = emptyList()
    private var searchJob: Job? = null

    init {
        loadCourses()
        loadInitial()
    }

    private fun loadCourses() {
        coursesRepository.getCachedList()?.let { publishCourseOptions(it) }
        viewModelScope.launch {
            runCatching { coursesRepository.refreshList() }
                .onSuccess { publishCourseOptions(it) }
        }
    }

    private fun publishCourseOptions(courses: List<Course>) {
        _courses.value = courses.map { course ->
            CourseFilterOption(
                courseId = course.courseId,
                label = course.courseName.ifBlank { course.courseFullName }
            )
        }
    }

    private fun loadInitial() {
        if (tryComposeFromCache()) {
            if (!repository.isBaseListFresh()) refreshSilently()
        } else {
            load(force = true)
        }
    }

    fun load(force: Boolean = true) {
        if (force) _displayItems.value = UiState.Loading
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

    fun manualRefresh() {
        if (_refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                rawItems = loadItems()
                publishDisplayItems()
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun refreshSilently() {
        viewModelScope.launch {
            runCatching { loadItems() }
                .onSuccess { items ->
                    rawItems = items
                    publishDisplayItems()
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
        if (isDefaultServerFilters() && tryComposeFromCache()) return
        load()
    }

    private fun publishDisplayItems() {
        if (_displayItems.value is UiState.Error) return
        _displayItems.value = UiState.Success(applyEnrollmentFiltersAndSort(rawItems, _filters.value))
    }

    private fun isDefaultServerFilters(): Boolean {
        val f = _filters.value
        return f.displayStatus == EnrollmentDisplayStatus.ALL && f.courseId == "All"
    }

    private fun tryComposeFromCache(): Boolean {
        if (!isDefaultServerFilters()) return false
        val enrollments = repository.getCachedBaseList() ?: return false
        val students = studentsRepository.getCachedBaseList() ?: return false
        val courses = coursesRepository.getCachedList() ?: return false
        rawItems = composeItems(enrollments, students, courses)
        publishDisplayItems()
        return true
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
            studentsRepository.list(StudentListFilters(limit = 1000)).rows
        }
        val coursesDeferred = async { coursesRepository.list().rows }

        val enrollments = enrollmentsDeferred.await()
        val students = studentsDeferred.await()
        val courses = coursesDeferred.await()

        if (isDefaultServerFilters()) {
            repository.cacheBaseList(enrollments)
            studentsRepository.cacheBaseList(students)
            coursesRepository.cacheList(courses)
            publishCourseOptions(courses)
        }

        composeItems(enrollments, students, courses)
    }

    private fun composeItems(
        enrollments: List<Enrollment>,
        students: List<Student>,
        courses: List<Course>
    ): List<EnrollmentListItem> {
        val studentsById = students.associateBy { it.studentId }
        val coursesById = courses.associateBy { it.courseId }
        return enrollments.map { it.toListItem(studentsById, coursesById) }
    }

    private fun Enrollment.toListItem(
        studentsById: Map<String, Student>,
        coursesById: Map<String, Course>
    ): EnrollmentListItem {
        val studentName = studentName
            ?: studentsById[studentId]?.fullName
            ?: studentId
            ?: "Unknown student"
        return EnrollmentListItem(
            enrollment = this,
            studentName = studentName,
            courseName = displayCourseName(coursesById)
        )
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Refresh failed"
    }
}
