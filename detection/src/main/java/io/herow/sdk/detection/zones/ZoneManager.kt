package io.herow.sdk.detection.zones

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.database.HerowDatabaseHelper
import io.herow.sdk.detection.geofencing.GeofencingReceiver
import io.herow.sdk.detection.helpers.GeofencingHelper
import io.herow.sdk.detection.location.LocationListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ZoneManager(
    val context: Context,
    private var zones: ArrayList<Zone>
) : CacheListener, LocationListener {

    companion object {
        private const val GEOFENCE_REQUEST_CODE = 1919
    }

    private val ioDispatcher = Dispatchers.IO

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val pendingIntent = createPendingIntent(context)
    private val zoneRepository = HerowDatabaseHelper.getZoneRepository(context)

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofencingReceiver::class.java)
        val pendingIntent = if (DeviceHelper.getCurrentAndroidVersion() < 30) {
            PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getBroadcast(
            context,
            GEOFENCE_REQUEST_CODE,
            intent,
            pendingIntent
        )
    }

    fun getZones(): ArrayList<Zone> {
        return zones
    }


    fun loadZones() {
        zones.clear()

        runBlocking {
            withContext(ioDispatcher) {
                retrieveZones().let { zones.addAll(it) }
            }
        }
        GlobalLogger.shared.info(context, "Zones from BDD are: $zones")
        updateGeofencesMonitoring()
    }

    override fun onCacheReception() {
        loadZones()
    }

    @SuppressLint("MissingPermission")
    private fun updateGeofencesMonitoring() {
        geofencingClient.removeGeofences(pendingIntent).run {
            addOnSuccessListener {
                GlobalLogger.shared.info(context, "Inside addOnSuccessListener")
                addGeofences()
            }
            addOnFailureListener {
                GlobalLogger.shared.error(
                    context,
                    "addOnFailureListener - An exception occurred: ${it.message}"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences() {
        if (zones.isNotEmpty()) {
            GlobalLogger.shared.info(context, "ZoneManager - Zones is not empty: $zones")

            val geofences = GeofencingHelper.buildGeofenceList(zones)
            val geofenceRequest = GeofencingHelper.getGeofencingRequest(geofences)

            geofencingClient.addGeofences(geofenceRequest, pendingIntent).run {
                addOnSuccessListener {
                    GlobalLogger.shared.info(
                        context,
                        "addOnSuccessListener - The geofences has been correctly added"
                    )
                    println("The geofences has been correctly added")
                }
                addOnFailureListener {
                    GlobalLogger.shared.error(
                        context,
                        "addOnFailureListener - An exception occurred: ${it.message}"
                    )
                    GlobalLogger.shared.error(
                        context,
                        "addOnFailureListener - An exception occurred: ${it.localizedMessage}"
                    )
                }
            }
        }
    }

    override fun onLocationUpdate(location: Location) {

        dispatchZonesAndNotification(location)
    }

    private fun retrieveZones(): ArrayList<Zone> {
        val zones: ArrayList<Zone>

        runBlocking {
            val zonesInDb = async(ioDispatcher) {
                zoneRepository.getAllZones() as ArrayList<Zone>
            }

            GlobalLogger.shared.info(null, "Inside coroutine for data")
            zones = zonesInDb.await()
        }

        GlobalLogger.shared.info(context, "Zones in DB are: $zones")

        return zones
    }

    fun dispatchZonesAndNotification(location: Location) {
        GlobalLogger.shared.info(context, "Inside dispatchZonesAndNotification() method")
        val detectedZones = ArrayList<Zone>()
        val detectedZoneForNotification = ArrayList<Zone>()

        synchronized(zones) {
            GlobalLogger.shared.info(context, "Zones synchronization: $zones")
            for (zone in ArrayList(zones)) {
                val zoneLocation = zone.toLocation()
                val distanceToCenterOfZone = location.distanceTo(zoneLocation)
                if (distanceToCenterOfZone - zone.radius!! <= 0) {
                    GlobalLogger.shared.info(context, "Adding zone: $zone")
                    detectedZones.add(zone)
                }

                if (distanceToCenterOfZone - zone.radius!! * 3 <= 0) {
                    GlobalLogger.shared.info(context, "Adding zone for radius x3: $zone")
                    detectedZoneForNotification.add(zone)
                }
            }
        }

        ZoneDispatcher.dispatchDetectedZones(detectedZones, location)
        ZoneDispatcher.dispatchDetectedZonesForNotification(detectedZoneForNotification, location)
        GlobalLogger.shared.info(
            context,
            "Zones dispatched: $detectedZones and location: $location"
        )
        GlobalLogger.shared.info(
            context,
            "Zones dispatched for notification: $detectedZoneForNotification"
        )
    }
}