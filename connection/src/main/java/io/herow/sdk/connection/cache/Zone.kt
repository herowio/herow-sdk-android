package io.herow.sdk.connection.cache

data class Zone(private val hash: String,
                private val lat: Double,
                private val lng: Double,
                private val radius: Int,
                private val access: Access)