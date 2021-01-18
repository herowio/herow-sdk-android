package io.herow.sdk.connection.entities.response.cache

data class Zone(private val hash: String,
                private val lat: Double,
                private val lng: Double,
                private val radius: Int,
                private val liveEvent: Boolean,
                private val access: Access)