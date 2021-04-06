package io.herow.sdk.detection.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.vmadalin.easypermissions.EasyPermissions
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.config.ConfigListener
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.geofencing.GeofenceEventGenerator
import io.herow.sdk.detection.zones.ZoneDispatcher
import io.herow.sdk.detection.zones.ZoneManager
import kotlinx.coroutines.*

class LocationManager(
    private val context: Context

) : ConfigListener, AppStateListener, LocationListener {

    companion object {
        private const val LOCATION_REQUEST_CODE = 1515
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private var isOnForeground: Boolean = false
    private var isGeofencingEnable: Boolean = false

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val zoneManager = ZoneManager(context, ArrayList())
    private val geofenceEventGenerator = GeofenceEventGenerator()
    private val pendingIntent = createPendingIntent(context)

    private val db: HerowDatabase = HerowDatabase.getDatabase(context)
    private var zones: List<Zone>? = null
    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            LOCATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    init {
        CacheDispatcher.addCacheListener(zoneManager)
        LocationDispatcher.addLocationListener(zoneManager)
        ZoneDispatcher.addZoneListener(geofenceEventGenerator)
        CoroutineScope(Dispatchers.IO).launch {
            zones =  zoneManager.getZones()

        }
    }

    override fun onConfigResult(configResult: ConfigResult) {
        synchronized(isGeofencingEnable) {
            isGeofencingEnable = configResult.isGeofenceEnable
        }
        if (isGeofencingEnable) {
            startMonitoring()
        } else {
            stopMonitoring()
        }
    }

    private fun checkForLocationsPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @SuppressLint("MissingPermission")
    private fun startMonitoring() {
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                LocationDispatcher.dispatchLocation(location)
            }
        }
        updateMonitoring()
    }

    /**
     * We want to update the monitoring based on the application state. In foreground, as the application
     * is currently being used by the user, we can make frequent location update. In background, we
     * only want to be able to track the user and thus improve native geofence performances.
     */
    @SuppressLint("MissingPermission")
    private fun updateMonitoring(location: Location? = null) {
            val locationRequest = buildLocationRequest(location)
            fusedLocationProviderClient.removeLocationUpdates(pendingIntent).addOnCompleteListener {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, pendingIntent)

        }
    }


    private fun buildLocationRequest(location: Location? = null): LocationRequest {
        zones =  zoneManager.getZones()
        val request = LocationRequest()
        var smallestDistance = Double.MAX_VALUE
        if (location != null && zones != null) {
            for (zone in zones!!) {
                var zoneCenter = Location("zone")
                zoneCenter.latitude = zone.lat ?: 0.0
                zoneCenter.longitude = zone.lng ?: 0.0
                val radius = zone.radius ?: 0.0
                val dist = zoneCenter.distanceTo(location)
                if (dist < smallestDistance) {
                    smallestDistance = maxOf((dist - radius), 0.0)
                }
            }
        }

        var priority = LocationRequest.PRIORITY_LOW_POWER
        var smallestDisplacement = smallestDistance / 10
        var interval = TimeHelper.FIVE_MINUTES_MS

        if (smallestDistance < 3000.0) {
            interval =  2 * TimeHelper.THREE_MINUTE_MS
            priority =  LocationRequest.PRIORITY_LOW_POWER
        }
        if (smallestDistance < 1000.0) {
            interval = TimeHelper.ONE_MINUTE_MS
            priority =  LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        if (smallestDistance < 500) {
            interval =   TimeHelper.TWENTY_SECONDS_MS
            priority =  LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        if (smallestDistance < 100.0) {
            interval =   TimeHelper.TEN_SECONDS_MS
            priority =  LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (smallestDistance < 50) {
            priority =  LocationRequest.PRIORITY_HIGH_ACCURACY
            interval =   TimeHelper.FIVE_SECONDS_MS
        }
        //TODO remove
        interval =   TimeHelper.FIVE_SECONDS_MS
        val smallestDisplacementtmp = 20.0

        request.smallestDisplacement = smallestDisplacementtmp.toFloat() //smallestDisplacement.toFloat()
        request.fastestInterval = TimeHelper.TWENTY_SECONDS_MS
        request.interval = interval
        request.priority = priority
        return request
    }

    private fun stopMonitoring() {
        fusedLocationProviderClient.removeLocationUpdates(pendingIntent)
            .addOnCompleteListener { task: Task<Void> ->
                if (task.isSuccessful) {
                    println("Stop making location update")
                }
            }
    }

    override fun onAppInForeground() {
        isOnForeground = true
        if (isGeofencingEnable) {
            updateMonitoring()
        }
    }

    override fun onAppInBackground() {
        isOnForeground = false
        if (isGeofencingEnable) {
            updateMonitoring()
        }
    }

    override fun onLocationUpdate(location: Location) {
        updateMonitoring(location)
        scope.launch {
            if (zoneManager.isZonesEmpty() || noDataInDB()) {
                HerowInitializer.getInstance(context).launchCacheRequest(location)
            }
        }
    }

    private suspend fun noDataInDB(): Boolean {
        val zoneRepository = ZoneRepository(db.zoneDAO())
        val poiRepository = PoiRepository(db.poiDAO())
        val campaignRepository = CampaignRepository(db.campaignDAO())

        val zonesInDb = scope.async(dispatcher) {
            zoneRepository.getAllZones()
        }

        val poisInDb = scope.async(dispatcher) {
            poiRepository.getAllPois()
        }

        val campaignsInDb = scope.async(dispatcher) {
            campaignRepository.getAllCampaigns()
        }

        return zonesInDb.await().isNullOrEmpty() && poisInDb.await()
            .isNullOrEmpty() && campaignsInDb.await().isNullOrEmpty()
    }
}