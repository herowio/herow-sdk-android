package io.herow.sdk.detection.zones

import io.herow.sdk.connection.cache.Zone
import java.util.concurrent.CopyOnWriteArrayList

object ZoneDispatcher {
    fun addZoneListener(zoneListener: ZoneListener) {
        zoneListeners.add(zoneListener)
    }
    private val zoneListeners = CopyOnWriteArrayList<ZoneListener>()

    fun dispatchNewZones(zones: List<Zone>) {
        for (zoneListener in zoneListeners) {
            zoneListener.onNewZonesReceive(zones)
        }
    }
}