package com.saicomputer.sms.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Cached<T>(
    val value: T,
    val savedAt: Long = System.currentTimeMillis()
)

/**
 * In-memory session-scoped cache for a single data type.
 * Cleared on logout / UNAUTHENTICATED via [SessionCacheRegistry].
 */
class SessionCache<T>(
    private val registry: SessionCacheRegistry
) : ClearableCache {
    private val _data = MutableStateFlow<Cached<T>?>(null)
    val flow: StateFlow<Cached<T>?> = _data.asStateFlow()

    val value: T? get() = _data.value?.value

    init {
        registry.register(this)
    }

    fun put(value: T) {
        _data.value = Cached(value)
    }

    fun isFresh(ttlMs: Long = DEFAULT_TTL_MS): Boolean {
        val cached = _data.value ?: return false
        return System.currentTimeMillis() - cached.savedAt < ttlMs
    }

    override fun clear() {
        _data.value = null
    }

    companion object {
        const val DEFAULT_TTL_MS = 10_000L
    }
}

/**
 * In-memory session-scoped cache keyed by [K] (e.g. one entry per opened detail ID).
 * Cleared on logout / UNAUTHENTICATED via [SessionCacheRegistry].
 */
class KeyedSessionCache<K, V>(
    private val registry: SessionCacheRegistry
) : ClearableCache {
    private val _data = MutableStateFlow<Map<K, Cached<V>>>(emptyMap())
    val flow: StateFlow<Map<K, Cached<V>>> = _data.asStateFlow()

    init {
        registry.register(this)
    }

    fun get(key: K): V? = _data.value[key]?.value

    fun put(key: K, value: V) {
        _data.value = _data.value + (key to Cached(value))
    }

    fun isFresh(key: K, ttlMs: Long = SessionCache.DEFAULT_TTL_MS): Boolean {
        val cached = _data.value[key] ?: return false
        return System.currentTimeMillis() - cached.savedAt < ttlMs
    }

    override fun clear() {
        _data.value = emptyMap()
    }
}
