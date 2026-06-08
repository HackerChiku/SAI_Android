package com.saicomputer.sms.feature.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.EnrollmentPreviewInput
import com.saicomputer.sms.data.dto.EnrollmentPreviewResult
import com.saicomputer.sms.data.dto.InstallmentCreateRow
import com.saicomputer.sms.data.dto.InstallmentEnrollmentCreateInput
import com.saicomputer.sms.data.dto.StudentListFilters
import com.saicomputer.sms.data.dto.SubscriptionEnrollmentCreateInput
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.InstallmentType
import com.saicomputer.sms.data.model.Student
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.CoursesRepository
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WizardState(
    val step: Int = 0,
    val skipStudentStep: Boolean = false,
    val studentId: String = "",
    val selectedStudent: Student? = null,
    val studentSearch: String = "",
    val students: List<Student> = emptyList(),
    val studentsLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val coursesLoading: Boolean = true,
    val selectedCourse: Course? = null,
    val startDate: String = LocalDate.now().toString(),
    val installmentType: InstallmentType = InstallmentType.Monthly,
    val subscriptionEndDate: String = "",
    val previewLoading: Boolean = false,
    val preview: EnrollmentPreviewResult? = null,
    val effectiveFee: Int = 0,
    val effectiveEnrollmentFee: Int = 0,
    val rows: List<EditableInstallment> = emptyList(),
    val customizeFeesExpanded: Boolean = false,
    val backdateEnabled: Boolean = false,
    val effectiveCreatedAt: String? = null,
    val submitting: Boolean = false,
    val error: String? = null
) {
    val displayStep: Int get() = if (skipStudentStep) step + 1 else step + 1
    val canProceedFromStudent: Boolean get() = studentId.isNotBlank()
    val canProceedFromCourse: Boolean get() = selectedCourse != null
}

@HiltViewModel
class EnrollmentWizardViewModel @Inject constructor(
    private val studentsRepository: StudentsRepository,
    private val coursesRepository: CoursesRepository,
    private val enrollmentsRepository: EnrollmentsRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(WizardState())
    val state: StateFlow<WizardState> = _state.asStateFlow()
    val currentUser: StateFlow<User?> = session.currentUser

    private var initialized = false
    private var searchJob: Job? = null

    fun initialize(studentId: String?) {
        if (initialized) return
        initialized = true
        val preset = studentId?.takeIf { it.isNotBlank() }.orEmpty()
        val skipStudent = preset.isNotBlank()
        _state.update {
            it.copy(
                studentId = preset,
                skipStudentStep = skipStudent,
                step = if (skipStudent) 1 else 0
            )
        }
        viewModelScope.launch {
            try {
                val courses = coursesRepository.list().rows.filter { c -> c.isActive }
                _state.update { it.copy(courses = courses, coursesLoading = false) }
            } catch (e: ApiException) {
                _state.update { it.copy(coursesLoading = false, error = e.friendlyMessage()) }
            }
            if (skipStudent) {
                loadStudent(preset)
            } else {
                searchStudents("")
            }
        }
    }

    private suspend fun loadStudent(id: String) {
        runCatching { studentsRepository.get(id).student }.onSuccess { student ->
            _state.update { it.copy(selectedStudent = student, studentId = student.studentId) }
        }
    }

    fun update(transform: (WizardState) -> WizardState) = _state.update(transform)

    fun onStudentSearchChange(query: String) {
        _state.update { it.copy(studentSearch = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchStudents(query)
        }
    }

    private suspend fun searchStudents(query: String) {
        _state.update { it.copy(studentsLoading = true) }
        try {
            val rows = studentsRepository.list(
                StudentListFilters(
                    search = query,
                    limit = 50
                )
            ).rows
            _state.update { it.copy(students = rows, studentsLoading = false) }
        } catch (e: ApiException) {
            _state.update { it.copy(studentsLoading = false, error = e.friendlyMessage()) }
        }
    }

    fun selectStudent(student: Student) {
        _state.update {
            it.copy(
                selectedStudent = student,
                studentId = student.studentId,
                selectedCourse = null,
                preview = null,
                rows = emptyList()
            )
        }
    }

    fun selectCourse(course: Course) {
        _state.update {
            it.copy(
                selectedCourse = course,
                installmentType = InstallmentType.Monthly,
                effectiveFee = if (course.billingType == BillingType.Subscription) course.monthlyFee else course.fee,
                effectiveEnrollmentFee = course.enrollmentFee,
                preview = null,
                rows = emptyList()
            )
        }
    }

    fun goToStep(step: Int) = _state.update { it.copy(step = step) }

    fun nextFromStudent() {
        if (_state.value.canProceedFromStudent) {
            goToStep(1)
        }
    }

    fun nextFromCourse(onError: (String) -> Unit) {
        val s = _state.value
        val course = s.selectedCourse ?: return
        if (course.billingType == BillingType.Subscription) {
            val endDate = s.subscriptionEndDate.ifBlank {
                s.preview?.suggestedExpectedEndDate
                    ?: s.preview?.expectedEndDate
                    ?: s.startDate
            }
            _state.update { it.copy(subscriptionEndDate = endDate, step = 2) }
            return
        }
        loadPreview(onError)
    }

    fun loadPreview(onError: (String) -> Unit) {
        val s = _state.value
        val course = s.selectedCourse ?: return
        _state.update { it.copy(previewLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val preview = enrollmentsRepository.preview(
                    EnrollmentPreviewInput(
                        studentId = s.studentId,
                        courseId = course.courseId,
                        startDate = s.startDate,
                        installmentType = s.installmentType,
                        effectiveFee = s.effectiveFee,
                        effectiveEnrollmentFee = s.effectiveEnrollmentFee
                    )
                )
                _state.update {
                    it.copy(
                        previewLoading = false,
                        preview = preview,
                        effectiveEnrollmentFee = if (preview.enrollmentFeeWaived) {
                            0
                        } else {
                            preview.suggestedEnrollmentFee.takeIf { f -> f > 0 } ?: it.effectiveEnrollmentFee
                        },
                        rows = preview.installments.map { r ->
                            EditableInstallment(amountDue = r.amountDue, dueDate = r.dueDate)
                        },
                        step = 2
                    )
                }
            } catch (e: ApiException) {
                _state.update { it.copy(previewLoading = false) }
                onError(e.friendlyMessage())
            }
        }
    }

    fun refreshPreview(onError: (String) -> Unit) {
        val s = _state.value
        val course = s.selectedCourse ?: return
        if (course.billingType == BillingType.Subscription) return
        _state.update { it.copy(previewLoading = true) }
        viewModelScope.launch {
            try {
                val preview = enrollmentsRepository.preview(
                    EnrollmentPreviewInput(
                        studentId = s.studentId,
                        courseId = course.courseId,
                        startDate = s.startDate,
                        installmentType = s.installmentType,
                        effectiveFee = s.effectiveFee,
                        effectiveEnrollmentFee = s.effectiveEnrollmentFee
                    )
                )
                _state.update {
                    it.copy(
                        previewLoading = false,
                        preview = preview,
                        rows = preview.installments.map { r ->
                            EditableInstallment(amountDue = r.amountDue, dueDate = r.dueDate)
                        }
                    )
                }
            } catch (e: ApiException) {
                _state.update { it.copy(previewLoading = false) }
                onError(e.friendlyMessage())
            }
        }
    }

    fun submit(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val s = _state.value
        val course = s.selectedCourse ?: return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                val enrollment = if (course.billingType == BillingType.Subscription) {
                    enrollmentsRepository.create(
                        SubscriptionEnrollmentCreateInput(
                            studentId = s.studentId,
                            courseId = course.courseId,
                            startDate = s.startDate,
                            expectedEndDate = s.subscriptionEndDate.ifBlank { s.startDate },
                            effectiveMonthlyFee = if (s.effectiveFee > 0) s.effectiveFee else course.monthlyFee,
                            effectiveEnrollmentFee = s.effectiveEnrollmentFee,
                            isBackdate = if (s.backdateEnabled) true else null,
                            effectiveCreatedAt = if (s.backdateEnabled) s.effectiveCreatedAt else null
                        )
                    )
                } else {
                    enrollmentsRepository.create(
                        InstallmentEnrollmentCreateInput(
                            studentId = s.studentId,
                            courseId = course.courseId,
                            startDate = s.startDate,
                            effectiveFee = s.effectiveFee,
                            effectiveEnrollmentFee = s.effectiveEnrollmentFee,
                            installmentType = s.installmentType,
                            installments = s.rows.map { InstallmentCreateRow(it.amountDue, it.dueDate) },
                            isBackdate = if (s.backdateEnabled) true else null,
                            effectiveCreatedAt = if (s.backdateEnabled) s.effectiveCreatedAt else null
                        )
                    )
                }
                _state.update { it.copy(submitting = false) }
                onSuccess(enrollment.enrollmentId)
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false) }
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false) }
                onError(e.message ?: "Failed")
            }
        }
    }
}
