package io.herow.sdk.detection.zones

import io.herow.sdk.connection.cache.model.Zone

interface ZoneListener {
    fun detectedZones(zones: List<Zone>)
}