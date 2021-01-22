package io.herow.sdk.detection.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.zones.ZoneDispatcher
import io.herow.sdk.detection.zones.ZoneManager

class LocationManager(context: Context): AppStateListener, LocationListener {
    companion object {
        private const val REQUEST_CODE = 1515
    }
    private var isOnForeground: Boolean = false
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val zoneManager = ZoneManager(ArrayList())
    private val pendingIntent = createPendingIntent(context)

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    init {
        ZoneDispatcher.addZoneListener(zoneManager)
    }

    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location ->
            LocationDispatcher.dispatchLocation(location)
        }
        if (isOnForeground) {
            fusedLocationProviderClient.requestLocationUpdates(buildForegroundLocationRequest(), pendingIntent)
        } else {
            fusedLocationProviderClient.requestLocationUpdates(buildBackgroundLocationRequest(), pendingIntent)
        }
    }

    private fun buildForegroundLocationRequest(): LocationRequest {
        val request = LocationRequest()
        request.fastestInterval = TimeHelper.TWENTY_SECONDS_MS
        request.interval = TimeHelper.TEN_SECONDS_MS
        request.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        return request
    }

    private fun buildBackgroundLocationRequest(): LocationRequest {
        val request = LocationRequest()
        request.fastestInterval = TimeHelper.TEN_MINUTES_MS
        request.interval = TimeHelper.FIVE_MINUTES_MS
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return request
    }

    override fun onAppInForeground() {
        isOnForeground = true
    }

    override fun onAppInBackground() {
        isOnForeground = false
    }

    override fun onLocationUpdate(location: Location) {
        if (zoneManager.isZonesEmpty()) {
            HerowInitializer.launchCacheRequest(location)
        }
    }
}