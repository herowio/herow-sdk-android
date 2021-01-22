package io.herow.sdk.detection.location

import android.location.Location
import java.util.concurrent.CopyOnWriteArrayList

object LocationDispatcher {
    fun addLocationListener(locationListener: LocationListener) {
        locationListeners.add(locationListener)
    }
    private val locationListeners = CopyOnWriteArrayList<LocationListener>()

    fun dispatchLocation(location: Location) {
        for (locationListener in locationListeners) {
            locationListener.onLocationUpdate(location)
        }
    }
}