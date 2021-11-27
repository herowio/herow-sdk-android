package io.herow.sdk.detection.zones

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Zone
import java.util.concurrent.CopyOnWriteArrayList

object ZoneDispatcher {

    fun addZoneListener(zoneListener: IZoneListener) {
        zoneListeners.add(zoneListener)
    }

    private val zoneListeners = CopyOnWriteArrayList<IZoneListener>()

    fun dispatchDetectedZones(zones: List<Zone>, location: Location) {
        GlobalLogger.shared.info(null, "Dispatching zones: $zones, and location: $location")

        for (zoneListener in zoneListeners) {
            zoneListener.detectedZones(zones, location)
        }
    }

    fun dispatchDetectedZonesForNotification(zonesForNotification: List<Zone>, location: Location) {
        GlobalLogger.shared.info(null, "Dispatching zones for notification: $zonesForNotification")

        for (zoneListener in zoneListeners) {
            zoneListener.detectedNotificationZones(zonesForNotification, location)
        }
    }
}