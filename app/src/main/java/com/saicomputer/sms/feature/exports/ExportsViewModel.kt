package com.saicomputer.sms.feature.exports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saicomputer.sms.core.network.ApiException
import com.saicomputer.sms.data.dto.ExportResponse
import com.saicomputer.sms.data.repo.ExportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExportKind { Students, Payments, Enrollments }

@HiltViewModel
class ExportsViewModel @Inject constructor(
    private val repository: ExportsRepository
) : ViewModel() {

    private val _busy = MutableStateFlow<ExportKind?>(null)
    val busy: StateFlow<ExportKind?> = _busy.asStateFlow()

    fun export(kind: ExportKind, onResult: (ExportResponse) -> Unit, onMessage: (String) -> Unit) {
        _busy.value = kind
        viewModelScope.launch {
            try {
                val res = when (kind) {
                    ExportKind.Students -> repository.students()
                    ExportKind.Payments -> repository.payments()
                    ExportKind.Enrollments -> repository.enrollments()
                }
                onResult(res)
            } catch (e: ApiException) {
                onMessage(e.friendlyMessage())
            } catch (e: Exception) {
                onMessage(e.message ?: "Export failed")
            } finally {
                _busy.value = null
            }
        }
    }
}
