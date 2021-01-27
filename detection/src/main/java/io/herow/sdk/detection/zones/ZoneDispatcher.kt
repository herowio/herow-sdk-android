package io.herow.sdk.detection.zones

import io.herow.sdk.connection.cache.model.Zone
import java.util.concurrent.CopyOnWriteArrayList

object ZoneDispatcher {
    fun addZoneListener(zoneListener: ZoneListener) {
        zoneListeners.add(zoneListener)
    }
    private val zoneListeners = CopyOnWriteArrayList<ZoneListener>()

    fun dispatchDetectedZones(zones: List<Zone>) {
        for (zoneListener in zoneListeners) {
            zoneListener.detectedZones(zones)
        }
    }
}