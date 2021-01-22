package io.herow.sdk.connection.config

import com.google.gson.annotations.SerializedName

data class ConfigResult(val cacheInterval: Long,
                        val configInterval: Long,
                        @SerializedName("enabled")
                        val isGeofenceEnable: Boolean)