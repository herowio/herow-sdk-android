package io.herow.sdk.detection.geofencing

interface GeofenceListener {
    fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>)
}