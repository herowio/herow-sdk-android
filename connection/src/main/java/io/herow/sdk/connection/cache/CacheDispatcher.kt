package io.herow.sdk.connection.cache

import io.herow.sdk.connection.cache.model.CacheResult
import java.util.concurrent.CopyOnWriteArrayList

object CacheDispatcher {
    fun addCacheListener(cacheListener: CacheListener) {
        cacheListeners.add(cacheListener)
    }
    private val cacheListeners = CopyOnWriteArrayList<CacheListener>()

    fun dispatch(cacheResult: CacheResult?) {
        for (cacheListener in cacheListeners) {
            cacheListener.onCacheReception(cacheResult!!)
        }
    }
}