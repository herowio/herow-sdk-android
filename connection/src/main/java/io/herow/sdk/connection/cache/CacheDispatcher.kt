package io.herow.sdk.connection.cache

import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

object CacheDispatcher {
    fun addCacheListener(cacheListener: CacheListener) {
        cacheListeners.add(cacheListener)
    }
    private val cacheListeners = CopyOnWriteArrayList<CacheListener>()

    fun dispatch() {
        for (cacheListener in cacheListeners) {
            Log.i("XXX/EVENT", "CacheDispatcher - Dispatching cache to: $cacheListeners")
            cacheListener.onCacheReception()
        }
    }
}