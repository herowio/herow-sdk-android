package io.herow.sdk.detection.geofencing

import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object GeofenceDispatcher {

    fun addGeofenceListener(geofenceListener: IGeofenceListener) {
        GlobalLogger.shared.info(null, "addGeofenceListener to (list): $geofenceListener")
        geofenceListeners.add(geofenceListener)
    }

    private val geofenceListeners = CopyOnWriteArrayList<IGeofenceListener>()

    fun dispatchGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        if (geofenceEvents.isEmpty()) {
            return
        }
        GlobalLogger.shared.info(null, "Dispatching geofenceEvents: $geofenceEvents")
        GlobalLogger.shared.info(null, "Dispatching geofence to (list): $geofenceListeners")
        for (geofenceListener in geofenceListeners) {
            GlobalLogger.shared.info(null, "Dispatching geofence to: $geofenceListener")
            geofenceListener.onGeofenceEvent(geofenceEvents)
        }
    }

    fun reset() {
        geofenceListeners.clear()
    }
}