package io.herow.sdk.common.helpers

import android.location.Location

data class LocationMapper(
    var speed: Float = 0F,
    var horizontalAccuracy: Float = 0F,
    var lng: Double = 0.0,
    var lat: Double = 0.0,
    var timestamp: Long = 0L
)

fun toLocationMapper(location: Location): LocationMapper = LocationMapper(
    location.speed,
    location.accuracy,
    location.longitude,
    location.latitude,
    location.time
)
