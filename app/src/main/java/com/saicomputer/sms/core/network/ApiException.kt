package com.saicomputer.sms.core.network

import kotlinx.serialization.json.JsonElement

/**
 * Thrown by [ApiClient] when the backend returns `{ ok: false, error: {...} }`
 * or when a transport-level failure occurs.
 */
class ApiException(
    val code: String,
    override val message: String,
    val details: JsonElement? = null
) : Exception(message) {

    val isUnauthenticated: Boolean get() = code == ErrorCode.UNAUTHENTICATED

    /** A user-friendly message suitable for showing in a Snackbar / error state. */
    fun friendlyMessage(): String = when (code) {
        ErrorCode.UNAUTHENTICATED -> "Your session has expired. Please sign in again."
        ErrorCode.FORBIDDEN -> "You do not have permission to do that."
        ErrorCode.NOT_FOUND -> "The requested item could not be found."
        ErrorCode.VALIDATION_ERROR -> message
        ErrorCode.CONFLICT -> message
        ErrorCode.RATE_LIMITED -> "Too many requests. Please wait a moment and try again."
        ErrorCode.NETWORK -> "Network error. Check your connection and try again."
        else -> message.ifBlank { "Something went wrong. Please try again." }
    }
}

object ErrorCode {
    const val UNAUTHENTICATED = "UNAUTHENTICATED"
    const val FORBIDDEN = "FORBIDDEN"
    const val NOT_FOUND = "NOT_FOUND"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val CONFLICT = "CONFLICT"
    const val RATE_LIMITED = "RATE_LIMITED"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
    const val NETWORK = "NETWORK"
}
