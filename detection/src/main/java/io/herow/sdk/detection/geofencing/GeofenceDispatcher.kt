package io.herow.sdk.detection.geofencing

import java.util.concurrent.CopyOnWriteArrayList

object GeofenceDispatcher {
    fun addGeofenceListener(geofenceListener: GeofenceListener) {
        geofenceListeners.add(geofenceListener)
    }
    private val geofenceListeners = CopyOnWriteArrayList<GeofenceListener>()

    fun dispatchGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        for (geofenceListener in geofenceListeners) {
            geofenceListener.onGeofenceEvent(geofenceEvents)
        }
    }
}