package com.saicomputer.sms.feature.enrollments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.CancelEnrollmentInput
import com.saicomputer.sms.data.dto.EditEndDateInput
import com.saicomputer.sms.data.dto.EditInstallmentsInput
import com.saicomputer.sms.data.dto.EnrollmentGetResponse
import com.saicomputer.sms.data.dto.ExtendSubscriptionInput
import com.saicomputer.sms.data.dto.InstallmentEditRow
import com.saicomputer.sms.data.dto.MarkCompleteInput
import com.saicomputer.sms.data.dto.MarkTopicCompleteInput
import com.saicomputer.sms.data.dto.SetExcludedFromBillingInput
import com.saicomputer.sms.data.dto.UnmarkTopicInput
import com.saicomputer.sms.data.model.Payment
import com.saicomputer.sms.data.model.ReceiptDetail
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.EnrollmentsRepository
import com.saicomputer.sms.data.repo.ReceiptsRepository
import com.saicomputer.sms.data.repo.SubscriptionsRepository
import com.saicomputer.sms.data.repo.TopicsRepository
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
class EnrollmentDetailViewModel @Inject constructor(
    private val repository: EnrollmentsRepository,
    private val topicsRepository: TopicsRepository,
    private val subscriptionsRepository: SubscriptionsRepository,
    private val receiptsRepository: ReceiptsRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<EnrollmentGetResponse>>(UiState.Loading)
    val state: StateFlow<UiState<EnrollmentGetResponse>> = _state.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    private var enrollmentId: String = ""

    fun load(id: String) {
        enrollmentId = id
        val cached = repository.getCachedEnrollment(id)
        if (cached != null) {
            _state.value = UiState.Success(cached)
            if (!repository.isEnrollmentFresh(id)) refreshSilently(id)
        } else {
            fetch(id)
        }
    }

    private fun fetch(id: String) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshEnrollment(id))
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage(), e.code)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    private fun refreshSilently(id: String) {
        viewModelScope.launch {
            runCatching { repository.refreshEnrollment(id) }
                .onSuccess { if (enrollmentId == id) _state.value = UiState.Success(it) }
        }
    }

    fun manualRefresh() {
        val id = enrollmentId
        if (id.isBlank() || _refreshing.value) return
        _refreshing.value = true
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.refreshEnrollment(id))
            } catch (e: Exception) {
                _refreshError.emit(friendlyMessage(e))
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun reload() = fetch(enrollmentId)

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is ApiException -> error.friendlyMessage()
        else -> error.message ?: "Failed"
    }

    fun payments(): List<Payment> = (_state.value as? UiState.Success)?.data?.payments.orEmpty()

    private fun run(onMessage: (String) -> Unit, block: suspend () -> String) {
        viewModelScope.launch {
            try {
                onMessage(block())
                reload()
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            } catch (e: Exception) {
                onMessage(e.message ?: "Failed")
            }
        }
    }

    fun saveInstallments(rows: List<InstallmentEditRow>, onMessage: (String) -> Unit) =
        run(onMessage) {
            repository.editInstallments(EditInstallmentsInput(enrollmentId, rows))
            "Installments updated"
        }

    fun setExcluded(excluded: Boolean, onMessage: (String) -> Unit) = run(onMessage) {
        repository.setExcludedFromBilling(SetExcludedFromBillingInput(enrollmentId, excluded))
        if (excluded) "Excluded from billing tracking" else "Included in billing tracking"
    }

    fun cancel(reason: String?, onMessage: (String) -> Unit) = run(onMessage) {
        repository.cancel(CancelEnrollmentInput(enrollmentId, reason))
        "Enrollment cancelled"
    }

    fun markComplete(
        force: Boolean,
        isBackdate: Boolean,
        effectiveActualEndDate: String?,
        onIncompleteTopics: (List<String>) -> Unit,
        onMessage: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = repository.markComplete(
                    MarkCompleteInput(
                        enrollmentId = enrollmentId,
                        forceComplete = if (force) true else null,
                        isBackdate = if (isBackdate) true else null,
                        effectiveActualEndDate = if (isBackdate) effectiveActualEndDate else null
                    )
                )
                if (res.requiresConfirmation) {
                    onIncompleteTopics(res.details?.incompleteTopics?.map { it.topicName } ?: emptyList())
                } else {
                    onMessage("Enrollment marked complete")
                    reload()
                }
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            } catch (e: Exception) {
                onMessage(e.message ?: "Failed")
            }
        }
    }

    fun markTopicComplete(topicId: String, notes: String?, onMessage: (String) -> Unit) = run(onMessage) {
        topicsRepository.markComplete(MarkTopicCompleteInput(topicId, notes))
        "Topic marked complete"
    }

    fun unmarkTopic(topicId: String, onMessage: (String) -> Unit) = run(onMessage) {
        topicsRepository.unmark(UnmarkTopicInput(topicId))
        "Topic unmarked"
    }

    fun extendSubscription(months: Int, onMessage: (String) -> Unit) = run(onMessage) {
        subscriptionsRepository.extend(ExtendSubscriptionInput(enrollmentId, months))
        "Subscription extended by $months month(s)"
    }

    fun editEndDate(newDate: String, onMessage: (String) -> Unit) = run(onMessage) {
        subscriptionsRepository.editEndDate(EditEndDateInput(enrollmentId, newDate))
        "End date updated"
    }

    fun loadReceipt(
        receiptId: String,
        onSuccess: (ReceiptDetail) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                onSuccess(receiptsRepository.get(receiptId).receipt)
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load receipt")
            }
        }
    }
}
