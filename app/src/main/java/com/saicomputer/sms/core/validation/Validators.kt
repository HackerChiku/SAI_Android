package com.saicomputer.sms.core.validation

import android.util.Patterns

/** Field-level validators (the analog of the web client's Zod schemas). */
object Validators {

    fun required(value: String?, field: String = "This field"): String? =
        if (value.isNullOrBlank()) "$field is required" else null

    fun phone(value: String?, required: Boolean = true): String? {
        val digits = value?.filter { it.isDigit() } ?: ""
        if (digits.isEmpty()) return if (required) "Phone number is required" else null
        return if (digits.length != 10) "Phone number must be exactly 10 digits" else null
    }

    fun email(value: String?, required: Boolean = false): String? {
        if (value.isNullOrBlank()) return if (required) "Email is required" else null
        return if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) "Enter a valid email" else null
    }

    /** Optional; if present must be exactly 12 digits. Masked input (with X) fails. */
    fun aadhaar(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val digits = value.filter { it.isDigit() }
        if (digits.length != value.length) return "Aadhaar must be 12 digits (no other characters)"
        return if (digits.length != 12) "Aadhaar must be exactly 12 digits" else null
    }

    fun positiveInt(value: String?, field: String = "Value"): String? {
        val n = value?.toIntOrNull()
        return when {
            n == null -> "$field must be a number"
            n <= 0 -> "$field must be greater than 0"
            else -> null
        }
    }

    fun digitsOnly(input: String, max: Int): String = input.filter { it.isDigit() }.take(max)
}
