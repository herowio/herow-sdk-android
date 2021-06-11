package io.herow.sdk.detection.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.IAppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.config.ConfigListener
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.detection.cache.CacheManager
import io.herow.sdk.detection.geofencing.GeofenceEventGenerator
import io.herow.sdk.detection.zones.ZoneDispatcher
import io.herow.sdk.detection.zones.ZoneManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LocationManager(
    private val context: Context,
    val sessionHolder: SessionHolder
) : ConfigListener, IAppStateListener, LocationListener, LocationPriorityListener {

    companion object {
        private const val LOCATION_REQUEST_CODE = 1515
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private var isOnForeground: Boolean = false
    private var isGeofencingEnable: Boolean = false

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val zoneManager = ZoneManager(context, ArrayList())
    private val geofenceEventGenerator = GeofenceEventGenerator(sessionHolder)
    private val pendingIntent = createPendingIntent(context)

    private var zones: List<Zone>? = null
    @SuppressLint("InlinedApi")
    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        val pendingIntent = if (DeviceHelper.getCurrentAndroidVersion() < 30) {
            PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getBroadcast(
            context,
            LOCATION_REQUEST_CODE,
            intent,
            pendingIntent
        )
    }

    private val cacheManager = CacheManager(context)

    init {
        CacheDispatcher.addCacheListener(zoneManager)
        LocationDispatcher.addLocationListener(zoneManager)
        ZoneDispatcher.addZoneListener(geofenceEventGenerator)
        CoroutineScope(Dispatchers.IO).launch {
            zones = zoneManager.getZones()

        }
        LocationPriorityDispatcher.registerLocationPriorityListener(this)
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

    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
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
        zones = zoneManager.getZones()
        var smallestDistance = Double.MAX_VALUE
        if (location != null && zones != null) {
            for (zone in zones!!) {
                val zoneCenter = Location("zone")
                zoneCenter.latitude = zone.lat ?: 0.0
                zoneCenter.longitude = zone.lng ?: 0.0
                val radius = zone.radius ?: 0.0
                val dist = zoneCenter.distanceTo(location)
                if (dist < smallestDistance) {
                    smallestDistance = maxOf((dist - radius), 0.0)
                }
            }
        }
        GlobalLogger.shared.debug(
            null,
            "dispatch location for dispatcher with distance : $smallestDistance"
        )
        LocationPriorityDispatcher.dispatchPriorityForDistance(smallestDistance)
    }

    @SuppressLint("MissingPermission")
    private fun updateMonitoring(priority: LocationPriority) {
        GlobalLogger.shared.debug(null, "update priority  : $priority")
        val locationRequest = buildLocationRequest(priority)
        fusedLocationProviderClient.removeLocationUpdates(pendingIntent).addOnCompleteListener {
            GlobalLogger.shared.debug(null, "relaunch with priority : $priority")
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, pendingIntent)
        }
    }

    private fun buildLocationRequest(priority: LocationPriority): LocationRequest {
        val request = LocationRequest()
        request.smallestDisplacement = priority.smallestDistance.toFloat()
        request.fastestInterval = TimeHelper.TEN_SECONDS_MS
        request.interval = priority.interval
        request.priority = priority.priority
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

    override fun onLocationPriority(priority: LocationPriority) {
        updateMonitoring(priority)
    }

    override fun onAppInForeground() {
        isOnForeground = true
        LocationPriorityDispatcher.setOnForeGround(isOnForeground)
        if (isGeofencingEnable) {
            updateMonitoring()
        }
    }

    override fun onAppInBackground() {
        isOnForeground = false
        LocationPriorityDispatcher.setOnForeGround(isOnForeground)
        if (isGeofencingEnable) {
            updateMonitoring()
        }
    }

    override fun onLocationUpdate(location: Location) {
        updateMonitoring(location)

        if (sessionHolder.getUpdateCacheStatus()
            || cacheManager.isGeoHashUnknownOrDifferent(sessionHolder, location)
            || cacheManager.shouldFetchNow(sessionHolder)
        ) {
            scope.launch { CacheManager(context).launchCacheRequest(location) }
        }
    }
}