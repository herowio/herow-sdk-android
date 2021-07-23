package io.herow.sdk.connection.cache

import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object CacheDispatcher {

    fun addCacheListener(cacheListener: ICacheListener) {
        cacheListeners.add(cacheListener)
    }

    private val cacheListeners = CopyOnWriteArrayList<ICacheListener>()

    fun dispatch() {
        for (cacheListener in cacheListeners) {
            GlobalLogger.shared.info(
                null,
                "Dispatching cache to: $cacheListeners"
            )
            cacheListener.onCacheReception()
        }
    }
}