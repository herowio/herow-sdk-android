package io.herow.sdk.detection.cache

import io.herow.sdk.connection.cache.CacheResult
import java.util.concurrent.CopyOnWriteArrayList

object CacheDispatcher {
    fun addCacheListener(cacheListener: CacheListener) {
        cacheListeners.add(cacheListener)
    }
    private val cacheListeners = CopyOnWriteArrayList<CacheListener>()

    fun dispatchCacheResult(cacheResult: CacheResult) {
        for (cacheListener in cacheListeners) {
            cacheListener.onCacheReception(cacheResult)
        }
    }
}