package com.saicomputer.sms.data.repo

import com.saicomputer.sms.core.network.ApiClient
import com.saicomputer.sms.core.session.SessionManager
import com.saicomputer.sms.data.dto.ChangePasswordInput
import com.saicomputer.sms.data.dto.LoginInput
import com.saicomputer.sms.data.dto.LoginResponse
import com.saicomputer.sms.data.dto.MeResponse
import com.saicomputer.sms.data.dto.OkResponse
import com.saicomputer.sms.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiClient,
    private val session: SessionManager
) {
    suspend fun login(email: String, password: String): User {
        val res: LoginResponse = api.call("auth.login", LoginInput(email.trim(), password))
        session.setSession(res.sessionToken, res.user)
        return res.user
    }

    suspend fun changePassword(current: String, newPassword: String) {
        api.call<OkResponse, ChangePasswordInput>(
            "auth.changePassword",
            ChangePasswordInput(current, newPassword)
        )
        // After changing, refresh the user so mustChangePassword clears.
        runCatching { me() }
    }

    suspend fun me(): User {
        val res: MeResponse = api.call("auth.me")
        session.setUser(res.user)
        return res.user
    }

    suspend fun logout() {
        runCatching { api.call<OkResponse>("auth.logout") }
        session.clear()
    }
}
