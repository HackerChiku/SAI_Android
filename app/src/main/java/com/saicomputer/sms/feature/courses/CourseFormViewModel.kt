package com.saicomputer.sms.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.dto.BulkSaveCourseTopicsInput
import com.saicomputer.sms.data.dto.CourseCreateInput
import com.saicomputer.sms.data.dto.CourseTopicRow
import com.saicomputer.sms.data.dto.CourseUpdateInput
import com.saicomputer.sms.data.model.BillingType
import com.saicomputer.sms.data.model.PackageType
import com.saicomputer.sms.data.model.TopicDurationUnit
import com.saicomputer.sms.data.repo.CoursesRepository
import com.saicomputer.sms.data.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TopicEditorRow(
    val localId: String = UUID.randomUUID().toString(),
    val courseTopicId: String? = null,
    val topicName: String = "",
    val description: String = "",
    val durationValue: Int = 1,
    val durationUnit: TopicDurationUnit = TopicDurationUnit.Weeks
)

data class CourseFormState(
    val isEdit: Boolean = false,
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val courseName: String = "",
    val courseFullName: String = "",
    val description: String = "",
    val courseLink: String = "",
    val durationMonths: Int = 12,
    val fee: Int = 0,
    val enrollmentFee: Int = 0,
    val maxInstallments: Int = 12,
    val maxInstallmentsCeiling: Int = 24,
    val billingType: BillingType = BillingType.Installment,
    val monthlyFee: Int = 0,
    val packageType: PackageType = PackageType.NONE,
    val packagePaidMonths: Int = 3,
    val packageBonusMonths: Int = 1,
    val generateCertificate: Boolean = false,
    val hasTopics: Boolean = false,
    val topics: List<TopicEditorRow> = emptyList(),
    val error: String? = null
) {
    val topicErrors: Map<String, String>
        get() {
            val errs = mutableMapOf<String, String>()
            val seen = mutableSetOf<String>()
            topics.forEach { t ->
                if (t.topicName.isBlank()) errs[t.localId] = "Name required"
                else if (t.topicName.length > 200) errs[t.localId] = "Name too long"
                else if (!seen.add(t.topicName.lowercase())) errs[t.localId] = "Duplicate name"
                if (t.durationValue <= 0) errs[t.localId] = "Duration must be > 0"
            }
            return errs
        }

    val canSubmit: Boolean
        get() = courseName.isNotBlank() &&
            courseFullName.isNotBlank() &&
            durationMonths > 0 &&
            maxInstallments in 1..maxInstallmentsCeiling &&
            (!hasTopics || (topics.isNotEmpty() && topicErrors.isEmpty())) &&
            !submitting && !loading
}

@HiltViewModel
class CourseFormViewModel @Inject constructor(
    private val repository: CoursesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseFormState())
    val state: StateFlow<CourseFormState> = _state.asStateFlow()

    private var courseId: String? = null

    fun initialize(id: String?) {
        if (courseId == id && (id == null || _state.value.isEdit)) return
        courseId = id
        viewModelScope.launch {
            // ceiling for max-installments field is the course-level cap (1..24).
            runCatching { settingsRepository.refresh() }
            if (id == null) {
                _state.value = CourseFormState(isEdit = false)
            } else {
                _state.value = CourseFormState(isEdit = true, loading = true)
                loadCourse(id)
            }
        }
    }

    private suspend fun loadCourse(id: String) {
        try {
            val c = repository.get(id).course
            val topics = if (c.hasTopics) {
                runCatching { repository.listTopics(id).topics }.getOrDefault(emptyList())
                    .map {
                        TopicEditorRow(
                            courseTopicId = it.courseTopicId,
                            topicName = it.topicName,
                            description = it.description.orEmpty(),
                            durationValue = it.estimatedDurationValue,
                            durationUnit = it.estimatedDurationUnit
                        )
                    }
            } else emptyList()
            _state.update {
                it.copy(
                    loading = false,
                    courseName = c.courseName,
                    courseFullName = c.courseFullName,
                    description = c.description.orEmpty(),
                    courseLink = c.courseLink.orEmpty(),
                    durationMonths = c.durationMonths,
                    fee = c.fee,
                    enrollmentFee = c.enrollmentFee,
                    maxInstallments = c.maxInstallments,
                    billingType = c.billingType,
                    monthlyFee = c.monthlyFee,
                    packageType = c.packageType,
                    packagePaidMonths = c.packagePaidMonths ?: 3,
                    packageBonusMonths = c.packageBonusMonths ?: 1,
                    generateCertificate = c.generateCertificate,
                    hasTopics = c.hasTopics,
                    topics = topics
                )
            }
        } catch (e: ApiException) {
            _state.update { it.copy(loading = false, error = e.friendlyMessage()) }
        }
    }

    fun update(transform: (CourseFormState) -> CourseFormState) = _state.update(transform)

    fun setHasTopics(enabled: Boolean) = _state.update { st ->
        val topics = when {
            !enabled -> emptyList()
            st.topics.isEmpty() -> listOf(TopicEditorRow())
            else -> st.topics
        }
        st.copy(hasTopics = enabled, topics = topics)
    }

    fun addTopic() = _state.update { it.copy(topics = it.topics + TopicEditorRow()) }

    fun removeTopic(localId: String) =
        _state.update { it.copy(topics = it.topics.filterNot { t -> t.localId == localId }) }

    fun updateTopic(localId: String, transform: (TopicEditorRow) -> TopicEditorRow) =
        _state.update { st ->
            st.copy(topics = st.topics.map { if (it.localId == localId) transform(it) else it })
        }

    fun submit(onSaved: (String) -> Unit, onMessage: (String) -> Unit) {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                val savedId = if (s.isEdit) {
                    repository.update(
                        CourseUpdateInput(
                            courseId = courseId!!,
                            courseName = s.courseName.trim(),
                            courseFullName = s.courseFullName.trim(),
                            description = s.description.ifBlank { null },
                            courseLink = s.courseLink.ifBlank { null },
                            durationMonths = s.durationMonths,
                            fee = s.fee,
                            enrollmentFee = s.enrollmentFee,
                            maxInstallments = s.maxInstallments,
                            billingType = s.billingType,
                            monthlyFee = if (s.billingType == BillingType.Subscription) s.monthlyFee else 0,
                            packageType = s.packageType,
                            packagePaidMonths = if (s.packageType == PackageType.PACKAGE_3_1) s.packagePaidMonths else null,
                            packageBonusMonths = if (s.packageType == PackageType.PACKAGE_3_1) s.packageBonusMonths else null,
                            generateCertificate = s.generateCertificate,
                            hasTopics = s.hasTopics
                        )
                    ).courseId
                } else {
                    repository.create(
                        CourseCreateInput(
                            courseName = s.courseName.trim(),
                            courseFullName = s.courseFullName.trim(),
                            description = s.description.ifBlank { null },
                            courseLink = s.courseLink.ifBlank { null },
                            durationMonths = s.durationMonths,
                            fee = s.fee,
                            enrollmentFee = s.enrollmentFee,
                            maxInstallments = s.maxInstallments,
                            billingType = s.billingType,
                            monthlyFee = if (s.billingType == BillingType.Subscription) s.monthlyFee else 0,
                            packageType = s.packageType,
                            packagePaidMonths = if (s.packageType == PackageType.PACKAGE_3_1) s.packagePaidMonths else null,
                            packageBonusMonths = if (s.packageType == PackageType.PACKAGE_3_1) s.packageBonusMonths else null,
                            generateCertificate = s.generateCertificate,
                            hasTopics = s.hasTopics
                        )
                    ).courseId
                }

                if (s.hasTopics) {
                    repository.bulkSaveTopics(
                        BulkSaveCourseTopicsInput(
                            courseId = savedId,
                            topics = s.topics.map {
                                CourseTopicRow(
                                    courseTopicId = it.courseTopicId,
                                    topicName = it.topicName.trim(),
                                    description = it.description.ifBlank { null },
                                    estimatedDurationValue = it.durationValue,
                                    estimatedDurationUnit = it.durationUnit
                                )
                            }
                        )
                    )
                    onMessage("Course saved with ${s.topics.size} topics")
                } else {
                    onMessage("Course saved")
                }
                _state.update { it.copy(submitting = false) }
                onSaved(savedId)
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message ?: "Failed to save") }
            }
        }
    }
}
