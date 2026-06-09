package com.saicomputer.sms.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.AuthRepository
import com.saicomputer.sms.data.repo.HomeDataLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BootstrapState(
    val loading: Boolean = true,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val homeDataLoader: HomeDataLoader,
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
            if (!session.hasToken) {
                _bootstrap.value = BootstrapState(loading = false, user = null)
                return@launch
            }
            try {
                val user = authRepository.me()
                if (user.mustChangePassword) {
                    _bootstrap.value = BootstrapState(loading = false, user = user)
                    return@launch
                }
                preloadHomeData(user)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                session.clear()
                _bootstrap.value = BootstrapState(loading = false, user = null)
            }
        }
    }

    private suspend fun preloadHomeData(user: User) {
        _bootstrap.value = BootstrapState(loading = true, user = user, error = null)
        homeDataLoader.load(user.role).fold(
            onSuccess = {
                _bootstrap.value = BootstrapState(loading = false, user = user, error = null)
            },
            onFailure = { error ->
                val message = when (error) {
                    is ApiException -> error.friendlyMessage()
                    else -> error.message ?: "Failed to load data"
                }
                _bootstrap.value = BootstrapState(loading = false, user = user, error = message)
            }
        )
    }

    fun retry() {
        val user = _bootstrap.value.user ?: session.currentUser.value ?: return
        viewModelScope.launch {
            preloadHomeData(user)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
