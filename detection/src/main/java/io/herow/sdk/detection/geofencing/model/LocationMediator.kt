package io.herow.sdk.detection.geofencing.model

data class LocationMediator(
    var speed: Float,
    var horizontalAccuracy: Float,
    var lng: Double,
    var lat: Double,
    var timestamp: Long
)