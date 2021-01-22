package io.herow.sdk.detection.cache

import io.herow.sdk.connection.cache.CacheResult

interface CacheListener {
    fun onCacheReception(cacheResult: CacheResult)
}