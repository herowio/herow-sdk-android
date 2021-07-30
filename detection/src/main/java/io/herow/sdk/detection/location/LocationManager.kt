package io.herow.sdk.detection.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.IAppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.config.IConfigListener
import io.herow.sdk.detection.geofencing.GeofenceEventGenerator
import io.herow.sdk.detection.zones.ZoneDispatcher
import io.herow.sdk.detection.zones.ZoneManager
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocationManager(
    context: Context,
    val sessionHolder: SessionHolder
) : IConfigListener, IAppStateListener, ILocationPriorityListener, KoinComponent {

    companion object {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private val dispatcher: CoroutineDispatcher by inject()

    private var isOnForeground: Boolean = false
    private var isGeofencingEnable: Boolean = false

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val zoneManager = ZoneManager(context, ArrayList())
    private val geofenceEventGenerator = GeofenceEventGenerator(sessionHolder)
    private var locationCallback: LocationCallback
    private var zones: List<Zone>? = null


    init {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()) {
                    val lastLocation = locationResult.locations.last()
                    LocationDispatcher.dispatchLocation(lastLocation)
                    updateMonitoring(lastLocation)
                }
            }
        }
        CacheDispatcher.addCacheListener(zoneManager)
        LocationDispatcher.addLocationListener(zoneManager)
        ZoneDispatcher.addZoneListener(geofenceEventGenerator)

        CoroutineScope(dispatcher).launch {
            zones = zoneManager.getZones()
        }

        zoneManager.loadZones()
        LocationPriorityDispatcher.registerLocationPriorityListener(this)
    }

    override fun onConfigResult(configResult: ConfigResult) {
        GlobalLogger.shared.info(context = null, "Config Result is: $configResult")
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
        GlobalLogger.shared.debug(
            null,
            "startMonitoring"
        )
        val locationRequest = buildLocationRequest(LocationPriority.HIGH)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        GlobalLogger.shared.debug(null, "relaunch with priority : $priority")
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    private fun buildLocationRequest(priority: LocationPriority): LocationRequest {
        val request = LocationRequest.create()
        request.smallestDisplacement = priority.smallestDistance.toFloat()
        request.fastestInterval = TimeHelper.TEN_SECONDS_MS
        request.interval = priority.interval
        request.priority = priority.priority
        return request
    }

    private fun stopMonitoring() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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
}