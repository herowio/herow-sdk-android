package io.herow.sdk.detection

import io.herow.sdk.connection.cache.Zone

class ZoneManager(private val zones: ArrayList<Zone>) {
    fun addZones(newZones: List<Zone>) {
        zones.clear()
        zones.addAll(newZones)
    }
}