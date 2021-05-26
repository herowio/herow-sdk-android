package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator: ZoneListener {
    private var previousDetectedZones = ArrayList<Zone>()
    private var previousDetectedZonesForNotification = ArrayList<Zone>()

    private val confidenceToUpdate = 0.0

    override fun detectedZones(zones: List<Zone>, location: Location) {
        defineGeofenceEventType(zones, location, null, previousDetectedZones)
    }

    override fun detectedNotificationZones(zonesForNotification: List<Zone>, location: Location) {
        defineGeofenceEventType(zonesForNotification, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, previousDetectedZonesForNotification)
    }

    private fun defineGeofenceEventType(zones: List<Zone>, location: Location, type: GeofenceType?, previousZonesDetected: ArrayList<Zone>) {
        val liveEvents = ArrayList<GeofenceEvent>()
        GlobalLogger.shared.info(null,"Zones at start are: $zones")

        if (previousZonesDetected.isEmpty()) {
            GlobalLogger.shared.info(null,"Previous detected zone is empty")

            for (zone in zones) {
                GlobalLogger.shared.info(null,"Zone is: $zone")

                if (type != null) {
                    val geofenceEvent = GeofenceEvent(zone, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, confidenceToUpdate)
                    geofenceEvent.confidence = geofenceEvent.computeNotificationConfidence(location, zone)
                    liveEvents.add(geofenceEvent)
                } else {
                    val geofenceEvent = GeofenceEvent(zone, location, GeofenceType.ENTER, confidenceToUpdate)
                    geofenceEvent.confidence = geofenceEvent.computeEnterConfidence(location, zone)
                    liveEvents.add(GeofenceEvent(zone, location, GeofenceType.ENTER, confidenceToUpdate))
                }
            }
        } else {
            for (previousZone in previousZonesDetected) {
                GlobalLogger.shared.info(null, "Previous detected zone: $previousDetectedZones")
                GlobalLogger.shared.info(null, "Previous detected zone for notification: $previousDetectedZonesForNotification")
                GlobalLogger.shared.info(null, "Zones are: $zones")
                GlobalLogger.shared.info(null, "PreviousZone is: $previousZone")

                val exit = zones.none { z -> z.hash == previousZone.hash }
                if (exit) {
                    GlobalLogger.shared.info(null, "Adding zone - Type EXIT")
                    val geofenceEvent = GeofenceEvent(previousZone, location, GeofenceType.EXIT, confidenceToUpdate)
                    geofenceEvent.confidence = geofenceEvent.computeExitConfidence(location, previousZone)
                    liveEvents.add(geofenceEvent)
                }
            }

            for (newPlace in zones) {
                GlobalLogger.shared.info(null, "Zones are: $zones")
                GlobalLogger.shared.info(null,  "NewPlace is: $newPlace")

                if (type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
                    if (!previousDetectedZonesForNotification.contains(newPlace)) {
                        GlobalLogger.shared.info(null, "Adding zone - Type NOTIFICATION_ENTER")
                        liveEvents.add(GeofenceEvent(newPlace, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, confidenceToUpdate))
                    }
                } else {
                    if (!previousDetectedZones.contains(newPlace)) {
                        GlobalLogger.shared.info(null, "Adding zone - Type ENTER")
                        liveEvents.add(GeofenceEvent(newPlace, location, GeofenceType.ENTER, confidenceToUpdate))
                    }
                }
            }
        }

        if (type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
            previousDetectedZonesForNotification.clear()
            previousDetectedZonesForNotification.addAll(zones)
        } else {
            previousDetectedZones.clear()
            previousDetectedZones.addAll(zones)
        }

        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}