package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginInput(val email: String, val password: String)

@Serializable
data class LoginResponse(val sessionToken: String, val user: User)

@Serializable
data class MeResponse(val user: User)

@Serializable
data class ChangePasswordInput(val currentPassword: String, val newPassword: String)

@Serializable
data class OkResponse(val ok: Boolean = true)
