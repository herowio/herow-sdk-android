package io.herow.sdk.connection.cache

import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object CacheDispatcher {
    fun addCacheListener(cacheListener: CacheListener) {
        cacheListeners.add(cacheListener)
    }

    private val cacheListeners = CopyOnWriteArrayList<CacheListener>()

    fun dispatch() {
        for (cacheListener in cacheListeners) {
            GlobalLogger.shared.info(
                null,
                "CacheDispatcher",
                "dispatch",
                15,
                "Dispatching cache to: $cacheListeners"
            )
            cacheListener.onCacheReception()
        }
    }
}