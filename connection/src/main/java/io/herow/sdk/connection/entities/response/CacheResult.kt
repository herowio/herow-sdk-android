package io.herow.sdk.connection.entities.response

import io.herow.sdk.connection.entities.response.cache.Poi
import io.herow.sdk.connection.entities.response.cache.Zone

data class CacheResult(private val zones: List<Zone>,
                       private val pois: List<Poi>)