package com.saicomputer.sms.feature.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.ChangeStudentStatusInput
import com.saicomputer.sms.data.dto.ChangeStudentStatusResponse
import com.saicomputer.sms.data.dto.StudentGetResponse
import com.saicomputer.sms.data.dto.VoidPaymentInput
import com.saicomputer.sms.data.model.ManualStudentStatus
import com.saicomputer.sms.data.model.ReceiptDetail
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.PaymentsRepository
import com.saicomputer.sms.data.repo.ReceiptsRepository
import com.saicomputer.sms.data.repo.StudentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val repository: StudentsRepository,
    private val paymentsRepository: PaymentsRepository,
    private val receiptsRepository: ReceiptsRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<StudentGetResponse>>(UiState.Loading)
    val state: StateFlow<UiState<StudentGetResponse>> = _state.asStateFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    private var studentId: String = ""

    fun load(id: String) {
        studentId = id
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.get(id))
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage(), e.code)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun reload() = load(studentId)

    fun changeStatus(
        newStatus: ManualStudentStatus,
        reason: String?,
        onResult: (ChangeStudentStatusResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = repository.changeStatus(
                    ChangeStudentStatusInput(studentId, newStatus, reason)
                )
                onResult(res)
                reload()
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed")
            }
        }
    }

    fun voidPayment(
        paymentId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                paymentsRepository.void(VoidPaymentInput(paymentId, reason))
                onSuccess()
                reload()
            } catch (e: ApiException) {
                onError(e.friendlyMessage())
            } catch (e: Exception) {
                onError(e.message ?: "Failed to void payment")
            }
        }
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
