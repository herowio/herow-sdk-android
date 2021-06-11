package io.herow.sdk.detection.geofencing.model

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

fun LocationMapper.toLocation(locationMapper: LocationMapper): Location {
    val location = Location("")

    return location.apply {
        speed = locationMapper.speed
        horizontalAccuracy = locationMapper.horizontalAccuracy
        longitude = locationMapper.lng
        latitude = locationMapper.lat
        time = locationMapper.timestamp
    }

}
