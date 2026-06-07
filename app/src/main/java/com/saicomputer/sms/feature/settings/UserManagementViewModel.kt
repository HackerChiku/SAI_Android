package com.saicomputer.sms.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.data.dto.UserCreateInput
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import com.saicomputer.sms.data.repo.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val repository: UsersRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val state: StateFlow<UiState<List<User>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                _state.value = UiState.Success(repository.list().users)
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun createUser(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        onMessage: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.create(
                    UserCreateInput(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        password = password,
                        role = role,
                        mustChangePassword = true
                    )
                )
                onMessage("User created")
                load()
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            }
        }
    }

    fun resetPassword(userId: String, newPassword: String, onMessage: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.resetPassword(userId, newPassword)
                onMessage("Password reset (${res.sessionsRevoked} session(s) revoked)")
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            }
        }
    }
}
