package com.saicomputer.sms.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserRole { Owner, Admin, Receptionist }

@Serializable
data class User(
    @SerialName("UserID") val userId: String,
    @SerialName("FullName") val fullName: String,
    @SerialName("Email") val email: String,
    @SerialName("Role") val role: UserRole,
    @SerialName("MustChangePassword") val mustChangePassword: Boolean = false
)
