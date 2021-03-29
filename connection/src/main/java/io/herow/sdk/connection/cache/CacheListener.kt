package io.herow.sdk.connection.cache

import io.herow.sdk.connection.cache.model.CacheResult

interface CacheListener {
    fun onCacheReception(cacheResult: CacheResult?)
}