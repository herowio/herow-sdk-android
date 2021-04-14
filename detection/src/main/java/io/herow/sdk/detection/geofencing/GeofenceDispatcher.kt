package io.herow.sdk.detection.geofencing

import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object GeofenceDispatcher {
    fun addGeofenceListener(geofenceListener: GeofenceListener) {
        geofenceListeners.add(geofenceListener)
    }

    private val geofenceListeners = CopyOnWriteArrayList<GeofenceListener>()

    fun dispatchGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        for (geofenceListener in geofenceListeners) {
            GlobalLogger.shared.info(null,"Dispatching geofence to: $geofenceListener")
            geofenceListener.onGeofenceEvent(geofenceEvents)
        }
    }
}