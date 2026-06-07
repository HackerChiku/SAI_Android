package com.saicomputer.sms.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !submitting
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun login(onSuccess: (User) -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                val user = authRepository.login(current.email, current.password)
                _state.update { it.copy(submitting = false) }
                onSuccess(user)
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message ?: "Login failed") }
            }
        }
    }
}
