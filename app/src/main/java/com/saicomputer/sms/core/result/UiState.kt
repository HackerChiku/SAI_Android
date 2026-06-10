package com.saicomputer.sms.core.result

/**
 * Generic screen state for one-shot and cached list loads.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val code: String? = null) : UiState<Nothing>
}

inline fun <T> UiState<T>.onSuccess(block: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) block(data)
    return this
}

val <T> UiState<T>.dataOrNull: T?
    get() = (this as? UiState.Success)?.data
