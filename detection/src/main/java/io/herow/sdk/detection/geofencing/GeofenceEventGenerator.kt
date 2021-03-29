package io.herow.sdk.detection.geofencing

import android.location.Location
import android.util.Log
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator: ZoneListener {
    private var previousDetectedZones = ArrayList<Zone>()

    override fun detectedZones(zones: List<Zone>, location: Location) {
        val liveEvents = ArrayList<GeofenceEvent>()
        Log.i("XXX/EVENT", "GeofenceEventGenerator - Zones at start are: $zones")

        if (previousDetectedZones.isEmpty()) {
            Log.i("XXX/EVENT", "GeofenceEventGenerator - Previous detected zone is empty")
            for (zone in zones) {
                Log.i("XXX/EVENT", "GeofenceEventGenerator - Zone is: $zone")
                liveEvents.add(GeofenceEvent(zone, location, GeofenceType.ENTER))
            }
        } else {
            for (previousZone in previousDetectedZones) {
                Log.i(
                    "XXX/EVENT",
                    "GeofenceEventGenerator - Previous detected zone: $previousDetectedZones"
                )
                Log.i("XXX/EVENT", "GeofenceEventGenerator - Zones are: $zones")
                Log.i("XXX/EVENT", "GeofenceEventGenerator - PreviousZone is: $previousZone")
                if (!zones.contains(previousZone)) {
                    Log.i("XXX/EVENT", "GeofenceEventGenerator - Adding zone - Type EXIT")
                    liveEvents.add(GeofenceEvent(previousZone, location, GeofenceType.EXIT))
                }
            }

            for (newPlace in zones) {
                Log.i("XXX/EVENT", "GeofenceEventGenerator - Zones are: $zones")
                Log.i("XXX/EVENT", "GeofenceEventGenerator - NewPlace is: $newPlace")
                if (!previousDetectedZones.contains(newPlace)) {
                    Log.i("XXX/EVENT", "GeofenceEventGenerator - Adding zone - Type ENTER")
                    liveEvents.add(GeofenceEvent(newPlace, location, GeofenceType.ENTER))
                }
            }
        }

        previousDetectedZones.clear()
        previousDetectedZones.addAll(zones)

        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}