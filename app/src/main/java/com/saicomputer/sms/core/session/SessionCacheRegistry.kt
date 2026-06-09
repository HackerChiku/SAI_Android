package com.saicomputer.sms.core.session

import javax.inject.Inject
import javax.inject.Singleton

/** A session cache that can be wiped on logout. */
interface ClearableCache {
    fun clear()
}

/**
 * Tracks all session cache instances so they can be wiped on logout.
 */
@Singleton
class SessionCacheRegistry @Inject constructor() {

    private val caches = mutableListOf<ClearableCache>()

    @Synchronized
    fun register(cache: ClearableCache) {
        if (cache !in caches) caches.add(cache)
    }

    @Synchronized
    fun clearAll() {
        caches.forEach { it.clear() }
    }
}
