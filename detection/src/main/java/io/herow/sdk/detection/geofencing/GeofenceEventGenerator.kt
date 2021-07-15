package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.zones.ZoneListener

class GeofenceEventGenerator(val sessionHolder: SessionHolder) : ZoneListener {
    private var previousDetectedZones: ArrayList<Zone> = if (sessionHolder.hasPreviousZones()) {
        sessionHolder.getSavedPreviousZones()
    } else {
        ArrayList()
    }

    private var previousDetectedZonesForNotification: ArrayList<Zone> =
        if (sessionHolder.hasPreviousZonesForNotification()) {
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
<<<<<<< HEAD
        defineGeofenceEventType(
            zonesForNotification,
            location,
            GeofenceType.GEOFENCE_NOTIFICATION_ENTER,
            previousDetectedZonesForNotification
        )
=======
         defineGeofenceEventType(zonesForNotification, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, previousDetectedZonesForNotification)
>>>>>>> 9f8ac2c61912206e089d1dd0785a6f519a0e49b6
    }

    private fun defineGeofenceEventType(
        zones: List<Zone>,
        location: Location,
        type: GeofenceType?,
        previousZonesDetected: ArrayList<Zone>
    ) {
        val liveEvents = ArrayList<GeofenceEvent>()
        println("Zones received are: $zones")
<<<<<<< HEAD
        GlobalLogger.shared.info(null, "Zones at start are: $zones")
        GlobalLogger.shared.info(null, "Previous zones are: $previousZonesDetected")
=======
        //GlobalLogger.shared.info(null,"Zones at start are: $zones")
        GlobalLogger.shared.info(null,"Previous zones are: $previousZonesDetected")
>>>>>>> 9f8ac2c61912206e089d1dd0785a6f519a0e49b6
        println("Previous zones detected are: $previousZonesDetected")
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

<<<<<<< HEAD
        for (newPlace in zones) {
            GlobalLogger.shared.info(null, "Zones are: $zones")
            GlobalLogger.shared.info(null, "NewPlace is: $newPlace")
            GlobalLogger.shared.info(null, "considering type - $type")
            if (type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {

                val isNew = previousDetectedZonesForNotification.none {
                    it.hash == newPlace.hash
                }
                if (isNew) {
                    GlobalLogger.shared.info(null, "Adding zone - Type NOTIFICATION_ENTER")
                    val geofenceEvent = GeofenceEvent(
                        newPlace,
                        location,
                        GeofenceType.GEOFENCE_NOTIFICATION_ENTER,
                        confidenceToUpdate
                    )
                    geofenceEvent.confidence =
                        geofenceEvent.computeNotificationConfidence(location, newPlace)
                    liveEvents.add(geofenceEvent)
                }
            } else {
                val isNew = previousDetectedZones.none {
                    it.hash == newPlace.hash
                }

                if (isNew) {
                    GlobalLogger.shared.info(null, "Adding zone - Type ENTER")
                    val geofenceEvent =
                        GeofenceEvent(newPlace, location, GeofenceType.ENTER, confidenceToUpdate)
                    geofenceEvent.confidence =
                        geofenceEvent.computeEnterConfidence(location, newPlace)
                    liveEvents.add(geofenceEvent)
=======
            for (newPlace in zones) {
                GlobalLogger.shared.info(null, "GeofenceEventGenerator - Zones are: $zones")
                GlobalLogger.shared.info(null,  "GeofenceEventGenerator - NewPlace is: $newPlace")
                GlobalLogger.shared.info(null, "GeofenceEventGenerator - considering type - $type")
                if (type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {

                    val isNew =  previousDetectedZonesForNotification.none{
                        it.hash == newPlace.hash
                    }
                    if (isNew) {
                        GlobalLogger.shared.info(null, "GeofenceEventGenerator - Adding zone - Type NOTIFICATION_ENTER")
                        val geofenceEvent = GeofenceEvent(newPlace, location, GeofenceType.GEOFENCE_NOTIFICATION_ENTER, confidenceToUpdate)
                        geofenceEvent.confidence = geofenceEvent.computeNotificationConfidence(location, newPlace)
                        liveEvents.add(geofenceEvent)
                    }
                } else {
                    val isNew = previousDetectedZones.none{
                        it.hash == newPlace.hash
                    }

                    if (isNew) {
                        GlobalLogger.shared.info(null, "GeofenceEventGenerator - Adding zone - Type ENTER")
                        val geofenceEvent = GeofenceEvent(newPlace, location, GeofenceType.ENTER, confidenceToUpdate)
                        geofenceEvent.confidence = geofenceEvent.computeEnterConfidence(location, newPlace)
                        liveEvents.add(geofenceEvent)
                    }
>>>>>>> 9f8ac2c61912206e089d1dd0785a6f519a0e49b6
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

        GlobalLogger.shared.info(context = null, "GeofenceEventGenerator - LiveEvents are: $liveEvents")
        GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
    }
}