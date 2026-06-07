package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.data.dto.ResetPasswordInput
import com.saicomputer.sms.data.dto.ResetPasswordResponse
import com.saicomputer.sms.data.dto.UserCreateInput
import com.saicomputer.sms.data.dto.UserUpdateInput
import com.saicomputer.sms.data.dto.UsersListResponse
import com.saicomputer.sms.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val api: ApiClient
) {
    suspend fun list(): UsersListResponse = api.call("users.list")

    suspend fun create(input: UserCreateInput): User = api.call("users.create", input)

    suspend fun update(input: UserUpdateInput): User = api.call("users.update", input)

    suspend fun resetPassword(userId: String, newPassword: String): ResetPasswordResponse =
        api.call("users.resetPassword", ResetPasswordInput(userId, newPassword))
}
