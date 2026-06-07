package com.saicomputer.sms.feature.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.EnrollmentPreviewInput
import com.saicomputer.sms.data.dto.EnrollmentPreviewResult
import com.saicomputer.sms.data.dto.InstallmentCreateRow
import com.saicomputer.sms.data.dto.InstallmentEnrollmentCreateInput
import com.saicomputer.sms.data.dto.SubscriptionEnrollmentCreateInput
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.Course
import com.saicomputer.sms.data.model.InstallmentType
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.CoursesRepository
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WizardState(
    val step: Int = 0,
    val studentId: String = "",
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
    val backdateEnabled: Boolean = false,
    val effectiveCreatedAt: String? = null,
    val submitting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EnrollmentWizardViewModel @Inject constructor(
    private val coursesRepository: CoursesRepository,
    private val enrollmentsRepository: EnrollmentsRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(WizardState())
    val state: StateFlow<WizardState> = _state.asStateFlow()
    val currentUser: StateFlow<User?> = session.currentUser

    private var initialized = false

    fun initialize(studentId: String?) {
        if (initialized) return
        initialized = true
        _state.update { it.copy(studentId = studentId.orEmpty()) }
        viewModelScope.launch {
            try {
                val courses = coursesRepository.list().rows.filter { it.isActive }
                _state.update { it.copy(courses = courses, coursesLoading = false) }
            } catch (e: ApiException) {
                _state.update { it.copy(coursesLoading = false, error = e.friendlyMessage()) }
            }
        }
    }

    fun update(transform: (WizardState) -> WizardState) = _state.update(transform)

    fun selectCourse(course: Course) {
        _state.update {
            it.copy(
                selectedCourse = course,
                installmentType = InstallmentType.Monthly,
                effectiveFee = course.fee,
                effectiveEnrollmentFee = course.enrollmentFee
            )
        }
    }

    fun goToStep(step: Int) = _state.update { it.copy(step = step) }

    fun loadPreview(onError: (String) -> Unit) {
        val s = _state.value
        val course = s.selectedCourse ?: return
        if (course.billingType == BillingType.Subscription) {
            // No installment preview; jump straight to confirm.
            _state.update { it.copy(step = 2) }
            return
        }
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
                        effectiveEnrollmentFee = if (preview.enrollmentFeeWaived) 0 else preview.suggestedEnrollmentFee.takeIf { f -> f > 0 } ?: it.effectiveEnrollmentFee,
                        rows = preview.installments.map { r -> EditableInstallment(amountDue = r.amountDue, dueDate = r.dueDate) },
                        step = 2
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
