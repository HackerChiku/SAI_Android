package com.saicomputer.sms.feature.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.core.result.UiState
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.SubscriptionListFilters
import com.saicomputer.sms.data.model.SubscriptionListItem
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.repo.SubscriptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionsListViewModel @Inject constructor(
    private val repository: SubscriptionsRepository,
    session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<SubscriptionListItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<SubscriptionListItem>>> = _state.asStateFlow()

    private val _pendingOnly = MutableStateFlow(false)
    val pendingOnly: StateFlow<Boolean> = _pendingOnly.asStateFlow()

    val currentUser: StateFlow<User?> = session.currentUser

    init { load() }

    fun togglePendingOnly() {
        _pendingOnly.update { !it }
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                val res = repository.list(
                    SubscriptionListFilters(pendingOnly = _pendingOnly.value.takeIf { it })
                )
                _state.value = UiState.Success(res.rows)
            } catch (e: ApiException) {
                _state.value = UiState.Error(e.friendlyMessage())
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }
}
