package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.connection.cache.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator: ZoneListener {
    private val previousDetecedZones = ArrayList<Zone>()

    override fun detectedZones(zones: List<Zone>, location: Location) {
        val liveEvents = ArrayList<GeofenceEvent>()

        if (previousDetecedZones.isEmpty()) {
            for (zone in zones) {
                liveEvents.add(GeofenceEvent(zone, location, GeofenceType.ENTER))
            }
        } else {
            for (previousZone in previousDetecedZones) {
                if (!zones.contains(previousZone)) {
                    liveEvents.add(GeofenceEvent(previousZone, location, GeofenceType.EXIT))
                }
            }
            for (newPlace in zones) {
                if (!previousDetecedZones.contains(newPlace)) {
                    liveEvents.add(GeofenceEvent(newPlace, location, GeofenceType.ENTER))
                }
            }
        }

        previousDetecedZones.clear()
        previousDetecedZones.addAll(zones)

        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}