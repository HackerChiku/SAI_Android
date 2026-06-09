package com.saicomputer.sms.core.session

import android.content.SharedPreferences
import com.saicomputer.sms.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the session token and the current [User].
 *
 * The token is mirrored to [EncryptedSharedPreferences] (security intent of the
 * web client's `sessionStorage`) and held in memory for synchronous access by
 * [com.saicomputer.sms.core.network.ApiClient]. [clear] wipes both on explicit
 * logout and on any `UNAUTHENTICATED` response.
 */
@Singleton
class SessionManager @Inject constructor(
    private val prefs: SharedPreferences,
    private val cacheRegistry: SessionCacheRegistry
) {
    @Volatile
    var token: String? = prefs.getString(KEY_TOKEN, null)
        private set

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val hasToken: Boolean get() = !token.isNullOrBlank()

    fun setSession(token: String, user: User) {
        this.token = token
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _currentUser.value = user
    }

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clear() {
        token = null
        prefs.edit().remove(KEY_TOKEN).apply()
        _currentUser.value = null
        cacheRegistry.clearAll()
    }

    companion object {
        private const val KEY_TOKEN = "session_token"
    }
}
