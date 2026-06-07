package com.saicomputer.sms.data.dto

import com.saicomputer.sms.data.model.SettingEntry
import com.saicomputer.sms.data.model.User
import com.saicomputer.sms.data.model.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SettingsResponse(
    val total: Int = 0,
    val settings: List<SettingEntry> = emptyList()
)

@Serializable
data class SettingUpdateRow(
    @SerialName("Key") val key: String,
    @SerialName("Value") val value: String,
    @SerialName("Category") val category: String? = null,
    @SerialName("UsedIn") val usedIn: String? = null
)

@Serializable
data class SettingsUpdateInput(
    val updates: List<SettingUpdateRow>
)

@Serializable
data class SettingUpdateResult(
    val key: String,
    val ok: Boolean = false,
    val created: Boolean = false
)

@Serializable
data class SettingsUpdateResponse(
    val results: List<SettingUpdateResult> = emptyList()
)

@Serializable
data class FileUploadBase64Input(
    val filename: String,
    val mimeType: String,
    val base64: String
)

@Serializable
data class UploadFileResponse(
    val fileId: String? = null,
    val viewUrl: String? = null
)

@Serializable
data class UsersListResponse(
    val total: Int = 0,
    val users: List<User> = emptyList()
)

@Serializable
data class UserCreateInput(
    @SerialName("FullName") val fullName: String,
    @SerialName("Email") val email: String,
    @SerialName("Password") val password: String,
    @SerialName("Role") val role: UserRole,
    @SerialName("MustChangePassword") val mustChangePassword: Boolean = true
)

@Serializable
data class UserUpdateInput(
    @SerialName("UserID") val userId: String,
    @SerialName("FullName") val fullName: String? = null,
    @SerialName("Role") val role: UserRole? = null,
    @SerialName("IsActive") val isActive: Boolean? = null
)

@Serializable
data class ResetPasswordInput(
    @SerialName("UserID") val userId: String,
    @SerialName("NewPassword") val newPassword: String
)

@Serializable
data class ResetPasswordResponse(
    val ok: Boolean = true,
    @SerialName("UserID") val userId: String? = null,
    val sessionsRevoked: Int = 0
)
