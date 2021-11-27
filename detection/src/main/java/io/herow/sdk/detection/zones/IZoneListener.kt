package io.herow.sdk.detection.zones

import android.location.Location
import io.herow.sdk.connection.cache.model.Zone

interface IZoneListener {
    fun detectedZones(zones: List<Zone>, location: Location)
    fun detectedNotificationZones(zonesForNotification: List<Zone>, location: Location)
}