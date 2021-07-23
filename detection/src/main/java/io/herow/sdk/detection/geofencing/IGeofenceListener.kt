package io.herow.sdk.detection.geofencing

interface IGeofenceListener {
    fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>)
}