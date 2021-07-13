package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator(val sessionHolder: SessionHolder): ZoneListener {
    private var previousDetectedZones: ArrayList<Zone> = if (sessionHolder.hasPreviousZones()) {
        sessionHolder.getSavedPreviousZones()
    } else {
        ArrayList()
    }

    private var previousDetectedZonesForNotification: ArrayList<Zone> = if (sessionHolder.hasPreviousZonesForNotification()) {
        sessionHolder.getSavedPreviousZonesForNotification()
    } else {
        ArrayList()
    }

    private val confidenceToUpdate = -1.0

    override fun detectedZones(zones: List<Zone>, location: Location) {
        GlobalLogger.shared.info(null, "Into detectedZones method")
        defineGeofenceEventType(zones, location, null, previousDetectedZones)
    }

    override fun detectedNotificationZones(zonesForNotification: List<Zone>, location: Location) {
        defineGeofenceEventType(zonesForNotification, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, previousDetectedZonesForNotification)
    }

    private fun defineGeofenceEventType(zones: List<Zone>, location: Location, type: GeofenceType?, previousZonesDetected: ArrayList<Zone>) {
        val liveEvents = ArrayList<GeofenceEvent>()
        println("Zones received are: $zones")
        GlobalLogger.shared.info(null,"Zones at start are: $zones")
        GlobalLogger.shared.info(null,"Previous zones are: $previousZonesDetected")

        println("Previous zones detected are: $previousZonesDetected")
        if (previousZonesDetected.isEmpty()) {
            GlobalLogger.shared.info(null,"Previous detected zone is empty")
            for (zone in zones) {
                GlobalLogger.shared.info(null,"Zone is: $zone")

                if (type != null) {
                    val geofenceEvent = GeofenceEvent(zone, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, confidenceToUpdate)
                    geofenceEvent.confidence = geofenceEvent.computeNotificationConfidence(location, zone)
                    liveEvents.add(geofenceEvent)
                } else {
                    GlobalLogger.shared.info(null, "Into defineGeofenceEvent method")
                    val geofenceEvent = GeofenceEvent(zone, location, GeofenceType.ENTER, confidenceToUpdate)
                    geofenceEvent.confidence = geofenceEvent.computeEnterConfidence(location, zone)

                    GlobalLogger.shared.info(null, "GeofenceEvent for Enter is: $geofenceEvent")
                    liveEvents.add(geofenceEvent)
                }
            }
        } else {
            println("Type is: $type")
            if (type == null) {
                for (previousZone in previousZonesDetected) {
                    GlobalLogger.shared.info(null, "Previous detected zone: $previousDetectedZones")
                    GlobalLogger.shared.info(
                        null,
                        "Previous detected zone for notification: $previousDetectedZonesForNotification"
                    )
                    GlobalLogger.shared.info(null, "Zones are: $zones")
                    GlobalLogger.shared.info(null, "PreviousZone is: $previousZone")

                    val exit = zones.none { it.hash == previousZone.hash }
                    println("Exit value is: $exit")
                    if (exit) {
                        GlobalLogger.shared.info(null, "Adding zone - Type EXIT")
                        val geofenceEvent = GeofenceEvent(
                            previousZone,
                            location,
                            GeofenceType.EXIT,
                            confidenceToUpdate
                        )

                        geofenceEvent.confidence = geofenceEvent.computeExitConfidence(location, previousZone)
                        liveEvents.add(geofenceEvent)
                    }

                    println("Live events are: $liveEvents")
                }
            }

            for (newPlace in zones) {
                GlobalLogger.shared.info(null, "Zones are: $zones")
                GlobalLogger.shared.info(null,  "NewPlace is: $newPlace")

                if (type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
                    val isNew =  previousDetectedZonesForNotification.none{
                        it.hash == newPlace.hash
                    }
                    if (isNew) {
                        GlobalLogger.shared.info(null, "Adding zone - Type NOTIFICATION_ENTER")
                        val geofenceEvent = GeofenceEvent(newPlace, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, confidenceToUpdate)
                        geofenceEvent.confidence = geofenceEvent.computeNotificationConfidence(location, newPlace)
                        liveEvents.add(geofenceEvent)
                    }
                } else {
                    val isNew = previousDetectedZones.none{
                        it.hash == newPlace.hash
                    }

                    if (isNew) {
                        GlobalLogger.shared.info(null, "Adding zone - Type ENTER")
                        val geofenceEvent = GeofenceEvent(newPlace, location, GeofenceType.ENTER, confidenceToUpdate)
                        geofenceEvent.confidence = geofenceEvent.computeEnterConfidence(location, newPlace)
                        liveEvents.add(geofenceEvent)
                    }
                }
            }
        }

        if (type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
            previousDetectedZonesForNotification.clear()
            previousDetectedZonesForNotification.addAll(zones)

            sessionHolder.clearPreviousZonesForNotification()
            sessionHolder.savePreviousZonesForNotification(zones)
        } else {
            previousDetectedZones.clear()
            previousDetectedZones.addAll(zones)

            sessionHolder.clearPreviousZones()
            sessionHolder.savePreviousZones(zones)
        }

        GlobalLogger.shared.info(context = null, "LiveEvents are: $liveEvents")
        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}