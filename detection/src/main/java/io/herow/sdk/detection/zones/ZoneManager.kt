package io.herow.sdk.detection.zones

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import io.herow.sdk.connection.cache.CacheResult
import io.herow.sdk.connection.cache.Zone
import io.herow.sdk.detection.cache.CacheListener
import io.herow.sdk.detection.geofencing.GeofencingReceiver
import io.herow.sdk.detection.helpers.GeofencingHelper
import io.herow.sdk.detection.location.LocationListener

class ZoneManager(context: Context, private val zones: ArrayList<Zone>): CacheListener, LocationListener {
    companion object {
        private const val GEOFENCE_REQUEST_CODE = 1919
    }
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val pendingIntent = createPendingIntent(context)

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofencingReceiver::class.java)
        return PendingIntent.getBroadcast(context, GEOFENCE_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun isZonesEmpty(): Boolean {
        return zones.isEmpty()
    }

    override fun onCacheReception(cacheResult: CacheResult) {
        zones.clear()
        zones.addAll(cacheResult.zones)
        updateGeofencesMonitoring()
    }

    @SuppressLint("MissingPermission")
    private fun updateGeofencesMonitoring() {
        geofencingClient.removeGeofences(pendingIntent)?.run {
            addOnSuccessListener {
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
            addOnFailureListener {
                print("An exception occurred: ${it.message}")
            }
        }
    }

    override fun onLocationUpdate(location: Location) {
        val detectedZones = ArrayList<Zone>()
        synchronized(zones) {
            for (zone in zones) {
                val zoneLocation = zone.toLocation()
                val distanceToCenterOfZone = location.distanceTo(zoneLocation)
                if (distanceToCenterOfZone - zone.radius <= 0) {
                    detectedZones.add(zone)
                }
            }
        }
        ZoneDispatcher.dispatchDetectedZones(detectedZones, location)
    }
}