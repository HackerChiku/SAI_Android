package com.saicomputer.sms.core.auth

import android.content.Context
import com.saicomputer.sms.data.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SavedAccount(
    val email: String,
    val fullName: String,
    val lastUsedAt: Long = System.currentTimeMillis()
)

/**
 * Persists login identifiers (email + display name) for quick re-sign-in.
 * Passwords are never stored. Survives logout.
 */
@Singleton
class SavedAccountsStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<SavedAccount> = load().sortedByDescending { it.lastUsedAt }

    fun save(user: User) {
        val email = user.email.trim()
        if (email.isBlank()) return
        val now = System.currentTimeMillis()
        val updated = load()
            .filterNot { it.email.equals(email, ignoreCase = true) }
            .plus(SavedAccount(email = email, fullName = user.fullName, lastUsedAt = now))
            .sortedByDescending { it.lastUsedAt }
            .take(MAX_ACCOUNTS)
        persist(updated)
    }

    private fun load(): List<SavedAccount> {
        val raw = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<SavedAccount>>(raw) }
            .getOrDefault(emptyList())
    }

    private fun persist(accounts: List<SavedAccount>) {
        prefs.edit().putString(KEY_ACCOUNTS, json.encodeToString(accounts)).apply()
    }

    private companion object {
        const val PREFS_NAME = "sms_app_prefs"
        const val KEY_ACCOUNTS = "saved_login_accounts"
        const val MAX_ACCOUNTS = 5
    }
}
