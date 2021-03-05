package io.herow.sdk.connection.cache

import java.util.concurrent.CopyOnWriteArrayList

object CacheDispatcher {
    fun addCacheListener(cacheListener: CacheListener) {
        cacheListeners.add(cacheListener)
    }
    private val cacheListeners = CopyOnWriteArrayList<CacheListener>()

    fun dispatch() {
        for (cacheListener in cacheListeners) {
            cacheListener.onCacheReception()
        }
    }
}