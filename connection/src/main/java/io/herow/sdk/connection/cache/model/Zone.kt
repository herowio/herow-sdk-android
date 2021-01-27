package io.herow.sdk.connection.cache.model

import android.location.Location

data class Zone(val hash: String,
                val lat: Double,
                val lng: Double,
                val radius: Int,
                val access: Access
) {
    fun toLocation(): Location {
        val location = Location(hash)
        location.latitude = lat
        location.longitude = lng
        return location
    }
}