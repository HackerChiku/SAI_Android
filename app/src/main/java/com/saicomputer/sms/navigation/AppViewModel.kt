package com.saicomputer.sms.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BootstrapState(
    val loading: Boolean = true,
    val user: User? = null
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val session: SessionManager
) : ViewModel() {

    private val _bootstrap = MutableStateFlow(BootstrapState(loading = true))
    val bootstrap: StateFlow<BootstrapState> = _bootstrap.asStateFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    init {
        hydrate()
    }

    private fun hydrate() {
        viewModelScope.launch {
            if (session.hasToken) {
                try {
                    val user = authRepository.me()
                    _bootstrap.value = BootstrapState(loading = false, user = user)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    session.clear()
                    _bootstrap.value = BootstrapState(loading = false, user = null)
                }
            } else {
                _bootstrap.value = BootstrapState(loading = false, user = null)
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
