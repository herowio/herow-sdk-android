package io.herow.sdk.detection.zones

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.geofencing.GeofencingReceiver
import io.herow.sdk.detection.helpers.DefaultDispatcherProvider
import io.herow.sdk.detection.helpers.DispatcherProvider
import io.herow.sdk.detection.helpers.GeofencingHelper
import io.herow.sdk.detection.location.LocationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ZoneManager(
    context: Context,
    private val zones: ArrayList<Zone>,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) : CacheListener, LocationListener {
    companion object {
        private const val GEOFENCE_REQUEST_CODE = 1919
    }

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

    fun isZonesEmpty(): Boolean {
        return zones.isEmpty()
    }

    override fun onCacheReception() {
        zones.clear()

        retrieveZones()?.let { zones.addAll(it) }
        updateGeofencesMonitoring()
    }

    @SuppressLint("MissingPermission")
    private fun updateGeofencesMonitoring() {
        geofencingClient.removeGeofences(pendingIntent)?.run {
            addOnSuccessListener {
                addGeofences()
            }
            addOnFailureListener {
                print("An exception occurred: ${it.message}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofences() {
        if (zones.isNotEmpty()) {
            val geofences = GeofencingHelper.buildGeofenceList(zones)
            val geofenceRequest = GeofencingHelper.getGeofencingRequest(geofences)

            geofencingClient.addGeofences(geofenceRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    println("The geofences has been correctly added.")
                }
                addOnFailureListener {
                    println("An exception occurred: ${it.message}.")
                    println("An exception occurred: ${it.localizedMessage}.")
                }
            }
        }
    }

    override fun onLocationUpdate(location: Location) {
        val detectedZones = ArrayList<Zone>()
        synchronized(zones) {
            for (zone in zones) {
                val zoneLocation = zone.toLocation()
                val distanceToCenterOfZone = location.distanceTo(zoneLocation)
                if (distanceToCenterOfZone - zone.radius!! <= 0) {
                    detectedZones.add(zone)
                }
            }
        }
        ZoneDispatcher.dispatchDetectedZones(detectedZones, location)
    }

    private fun retrieveZones(): ArrayList<Zone>? {
        val zoneRepository = ZoneRepository(db.zoneDAO())
        var zonesInDB: ArrayList<Zone>? = null

        CoroutineScope(dispatcher.io()).launch {
            zonesInDB = zoneRepository.getAllZones() as ArrayList<Zone>
        }

        return zonesInDB
    }
}