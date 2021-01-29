package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator: ZoneListener {
    private val previousDetectedZones = ArrayList<Zone>()

    override fun detectedZones(zones: List<Zone>, location: Location) {
        val liveEvents = ArrayList<GeofenceEvent>()

        if (previousDetectedZones.isEmpty()) {
            for (zone in zones) {
                liveEvents.add(GeofenceEvent(zone, location, GeofenceType.ENTER))
            }
        } else {
            for (previousZone in previousDetectedZones) {
                if (!zones.contains(previousZone)) {
                    liveEvents.add(GeofenceEvent(previousZone, location, GeofenceType.EXIT))
                }
            }
            for (newPlace in zones) {
                if (!previousDetectedZones.contains(newPlace)) {
                    liveEvents.add(GeofenceEvent(newPlace, location, GeofenceType.ENTER))
                }
            }
        }

        previousDetectedZones.clear()
        previousDetectedZones.addAll(zones)

        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}