package io.herow.sdk.detection.zones

import io.herow.sdk.connection.cache.Zone

class ZoneManager(private val zones: ArrayList<Zone>): ZoneListener {
    fun isZonesEmpty(): Boolean {
        return zones.isEmpty()
    }

    override fun onNewZonesReceive(newZones: List<Zone>) {
        zones.clear()
        zones.addAll(newZones)
    }
}