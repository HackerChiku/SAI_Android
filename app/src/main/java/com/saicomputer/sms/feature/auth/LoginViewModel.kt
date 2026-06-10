package com.saicomputer.sms.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.auth.SavedAccount
import com.saicomputer.sms.core.auth.SavedAccountsStore
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

enum class LoginStep {
    AccountPicker,
    QuickSignIn,
    FullSignIn
}

data class LoginUiState(
    val step: LoginStep = LoginStep.FullSignIn,
    val savedAccounts: List<SavedAccount> = emptyList(),
    val selectedAccount: SavedAccount? = null,
    val email: String = "",
    val password: String = "",
    val submitting: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = when (step) {
            LoginStep.AccountPicker -> false
            LoginStep.QuickSignIn -> password.isNotBlank() && !submitting
            LoginStep.FullSignIn -> email.isNotBlank() && password.isNotBlank() && !submitting
        }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val savedAccountsStore: SavedAccountsStore
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        val saved = savedAccountsStore.getAll()
        _state.value = LoginUiState(
            step = if (saved.isNotEmpty()) LoginStep.AccountPicker else LoginStep.FullSignIn,
            savedAccounts = saved
        )
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    fun selectAccount(account: SavedAccount) {
        _state.update {
            it.copy(
                step = LoginStep.QuickSignIn,
                selectedAccount = account,
                email = account.email,
                password = "",
                error = null
            )
        }
    }

    fun chooseAnotherAccount() {
        _state.update {
            it.copy(
                step = LoginStep.FullSignIn,
                selectedAccount = null,
                email = "",
                password = "",
                error = null
            )
        }
    }

    fun backToAccountPicker() {
        val saved = savedAccountsStore.getAll()
        _state.update {
            it.copy(
                step = LoginStep.AccountPicker,
                savedAccounts = saved,
                selectedAccount = null,
                email = "",
                password = "",
                error = null
            )
        }
    }

    fun login(onSuccess: (User) -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            try {
                val user = authRepository.login(current.email, current.password)
                _state.update {
                    it.copy(
                        submitting = false,
                        savedAccounts = savedAccountsStore.getAll()
                    )
                }
                onSuccess(user)
            } catch (e: ApiException) {
                _state.update { it.copy(submitting = false, error = e.friendlyMessage()) }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message ?: "Login failed") }
            }
        }
    }
}
