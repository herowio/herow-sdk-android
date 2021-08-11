package io.herow.sdk.detection.zones

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.ICacheListener
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.detection.geofencing.GeofencingReceiver
import io.herow.sdk.detection.helpers.GeofencingHelper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.location.ILocationListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class ZoneManager(
    val context: Context,
    private var zones: ArrayList<Zone>
) : ICacheListener, ILocationListener, ICustomKoinComponent {

    companion object {
        private const val GEOFENCE_REQUEST_CODE = 1919
    }

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val pendingIntent = createPendingIntent(context)
    private val zoneRepository: ZoneRepository by inject()
    private val dispatcher: CoroutineDispatcher by inject()

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofencingReceiver::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
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
            withContext(dispatcher) {
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
            val zonesInDb = async(dispatcher) {
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
        var detectedZones = ArrayList<Zone>()
        var detectedZoneForNotification = ArrayList<Zone>()
        val zonesToCompute = ArrayList(zones)
        synchronized(zones) {
            detectedZones = ArrayList(zonesToCompute.filter { it.isIn(location) })
            detectedZoneForNotification =
                ArrayList(zonesToCompute.filter { it.isIn(location, 3.0) })

        }
        val names = detectedZones.map { it.access?.name }
        GlobalLogger.shared.info(null, "Zones at start are: $names")
        val notificationsnames = detectedZoneForNotification.map { it.access?.name }
        GlobalLogger.shared.info(null, "NotificationsZones at start are: $notificationsnames")
        ZoneDispatcher.dispatchDetectedZones(detectedZones, location)
        ZoneDispatcher.dispatchDetectedZonesForNotification(detectedZoneForNotification, location)
        detectedZones.clear()
        detectedZoneForNotification.clear()
    }
}