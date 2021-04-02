package io.herow.sdk.detection.geofencing.model

data class LocationMapper(
    var speed: Float,
    var horizontalAccuracy: Float,
    var lng: Double,
    var lat: Double,
    var timestamp: Long
)