package com.saicomputer.sms.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val submitting: Boolean = false,
    val error: String? = null
) {
    val newPasswordError: String?
        get() = if (newPassword.isNotEmpty() && newPassword.length < 8) {
            "Password must be at least 8 characters"
        } else null

    val confirmError: String?
        get() = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
            "Passwords do not match"
        } else null

    val canSubmit: Boolean
        get() = currentPassword.isNotBlank() &&
            newPassword.length >= 8 &&
            confirmPassword == newPassword &&
            !submitting
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun onCurrentChange(v: String) = _state.update { it.copy(currentPassword = v, error = null) }
    fun onNewChange(v: String) = _state.update { it.copy(newPassword = v, error = null) }
    fun onConfirmChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }

    fun submit(onChanged: () -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.changePassword(current.currentPassword, current.newPassword)
                _state.update { it.copy(submitting = false) }
                onChanged()
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message ?: "Failed") }
            }
        }
    }
}
