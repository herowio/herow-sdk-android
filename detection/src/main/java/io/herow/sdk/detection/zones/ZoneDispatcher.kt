package io.herow.sdk.detection.zones

import android.location.Location
import io.herow.sdk.connection.cache.Zone
import java.util.concurrent.CopyOnWriteArrayList

object ZoneDispatcher {
    fun addZoneListener(zoneListener: ZoneListener) {
        zoneListeners.add(zoneListener)
    }
    private val zoneListeners = CopyOnWriteArrayList<ZoneListener>()

    fun dispatchDetectedZones(zones: List<Zone>, location: Location) {
        for (zoneListener in zoneListeners) {
            zoneListener.detectedZones(zones, location)
        }
    }
}