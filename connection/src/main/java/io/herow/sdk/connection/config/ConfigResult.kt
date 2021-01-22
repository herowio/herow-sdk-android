package io.herow.sdk.connection.config

import com.squareup.moshi.Json

data class ConfigResult(val cacheInterval: Long,
                        val configInterval: Long,
                        @field:Json(name = "enabled")
                        val isGeofenceEnable: Boolean)