package com.saicomputer.sms.feature.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.ChangeStudentStatusInput
import com.saicomputer.sms.data.dto.ChangeStudentStatusResponse
import com.saicomputer.sms.data.dto.StudentGetResponse
import com.saicomputer.sms.data.model.ManualStudentStatus
import com.saicomputer.sms.data.model.User
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
}
