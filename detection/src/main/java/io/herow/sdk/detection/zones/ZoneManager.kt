package io.herow.sdk.detection.zones

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.geofencing.GeofencingReceiver
import io.herow.sdk.detection.helpers.GeofencingHelper
import io.herow.sdk.detection.location.LocationListener
import kotlinx.coroutines.*

class ZoneManager(
    val context: Context,
    private val zones: ArrayList<Zone>
) : CacheListener, LocationListener {

    companion object {
        private const val GEOFENCE_REQUEST_CODE = 1919
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private val ioDispatcher = Dispatchers.IO

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val pendingIntent = createPendingIntent(context)

    private val db: HerowDatabase = HerowDatabase.getDatabase(context)

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofencingReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            GEOFENCE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    public fun getZones(): ArrayList<Zone> {
        return zones
    }

    fun isZonesEmpty(): Boolean {
        return zones.isEmpty()
    }

    override fun onCacheReception() {
        zones.clear()

        runBlocking {
            withContext(ioDispatcher) {
                retrieveZones().let { zones.addAll(it) }
            }
        }
        GlobalLogger.shared.info(context, "ZoneManager", "onCacheReception", 60, "Zones from BDD are: $zones")
        updateGeofencesMonitoring()
    }

    @SuppressLint("MissingPermission")
    private fun updateGeofencesMonitoring() {
        geofencingClient.removeGeofences(pendingIntent)?.run {
            addOnSuccessListener {
                GlobalLogger.shared.info(context, "ZoneManager", "updateGeofencesMonitoring", 69, "Inside addOnSuccessListener")
                addGeofences()
            }
            addOnFailureListener {
                GlobalLogger.shared.error(context, "ZoneManager", "updateGeofencesMonitoring", 72, "addOnFailureListener - An exception occurred: ${it.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences() {
        if (zones.isNotEmpty()) {
            GlobalLogger.shared.info(context, "ZoneManager", "addGeofences", 80, "ZoneManager - Zones is not empty: $zones")

            val geofences = GeofencingHelper.buildGeofenceList(zones)
            val geofenceRequest = GeofencingHelper.getGeofencingRequest(geofences)

            geofencingClient.addGeofences(geofenceRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    GlobalLogger.shared.info(context, "ZoneManager", "addGeofences", 87, "addOnSuccessListener - The geofences has been correctly added")
                    println("The geofences has been correctly added")
                }
                addOnFailureListener {
                    GlobalLogger.shared.error(context, "ZoneManager", "addGeofences", 90, "addOnFailureListener - An exception occurred: ${it.message}")
                    GlobalLogger.shared.error(context, "ZoneManager", "addGeofences", 91, "addOnFailureListener - An exception occurred: ${it.localizedMessage}")
                }
            }
        }
    }

    override fun onLocationUpdate(location: Location) {
        GlobalLogger.shared.info(context, "ZoneManager", "onLocationUpdate", 99, "Inside onLocationUpdate method")
        val detectedZones = ArrayList<Zone>()
        synchronized(zones) {
            GlobalLogger.shared.info(context, "ZoneManager", "onLocationUpdate", 102, "Zones synchronization: $zones")
            for (zone in zones) {
                val zoneLocation = zone.toLocation()
                val distanceToCenterOfZone = location.distanceTo(zoneLocation)
                if (distanceToCenterOfZone - zone.radius!! <= 0) {
                    GlobalLogger.shared.info(context, "ZoneManager", "onLocationUpdate", 107, "Adding zone: $zone")
                    detectedZones.add(zone)
                }
            }
        }
        ZoneDispatcher.dispatchDetectedZones(detectedZones, location)
        GlobalLogger.shared.info(context, "ZoneManager", "onLocationUpdate", 113, "Zones dispatched: $detectedZones")
    }

    private suspend fun retrieveZones(): ArrayList<Zone> {
        val zoneRepository = ZoneRepository(db.zoneDAO())

        val zonesInDb = scope.async(ioDispatcher) {
            zoneRepository.getAllZones() as ArrayList<Zone>
        }

        return zonesInDb.await()
    }
}