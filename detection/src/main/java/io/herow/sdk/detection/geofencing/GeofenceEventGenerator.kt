package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator: ZoneListener {
    private var previousDetectedZones = ArrayList<Zone>()

    override fun detectedZones(zones: List<Zone>, location: Location) {
        val liveEvents = ArrayList<GeofenceEvent>()
        GlobalLogger.shared.info(null,  "Zones at start are: $zones")

        if (previousDetectedZones.isEmpty()) {
            GlobalLogger.shared.info(null, "Previous detected zone is empty")

            for (zone in zones) {
                GlobalLogger.shared.info(null, "Zone is: $zone")
                liveEvents.add(GeofenceEvent(zone, location, GeofenceType.ENTER))
            }
        } else {
            for (previousZone in previousDetectedZones) {
                GlobalLogger.shared.info(null, "Previous detected zone: $previousDetectedZones")
                GlobalLogger.shared.info(null, "Zones are: $zones")
                GlobalLogger.shared.info(null, "PreviousZone is: $previousZone")

                if (!zones.contains(previousZone)) {
                    GlobalLogger.shared.info(null, "Adding zone - Type EXIT")
                    liveEvents.add(GeofenceEvent(previousZone, location, GeofenceType.EXIT))
                }
            }

            for (newPlace in zones) {
                GlobalLogger.shared.info(null, "Zones are: $zones")
                GlobalLogger.shared.info(null,  "NewPlace is: $newPlace")

                if (!previousDetectedZones.contains(newPlace)) {
                    GlobalLogger.shared.info(null, "Adding zone - Type ENTER")
                    liveEvents.add(GeofenceEvent(newPlace, location, GeofenceType.ENTER))
                }
            }
        }

        previousDetectedZones.clear()
        previousDetectedZones.addAll(zones)

        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}