package io.herow.sdk.detection.zones

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Zone
import java.util.concurrent.CopyOnWriteArrayList

object ZoneDispatcher {
    fun addZoneListener(zoneListener: ZoneListener) {
        zoneListeners.add(zoneListener)
    }
    private val zoneListeners = CopyOnWriteArrayList<ZoneListener>()

    fun dispatchDetectedZones(zones: List<Zone>, location: Location) {
        GlobalLogger.shared.info(null,"Dispatching zones: $zones")
        for (zoneListener in zoneListeners) {
            zoneListener.detectedZones(zones, location)
        }
    }
}