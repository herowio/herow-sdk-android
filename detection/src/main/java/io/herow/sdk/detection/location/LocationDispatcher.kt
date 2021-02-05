package io.herow.sdk.detection.location

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import java.util.concurrent.CopyOnWriteArrayList

object LocationDispatcher {

    private var lastLocation: Location? = null

    fun addLocationListener(locationListener: LocationListener) {
        locationListeners.add(locationListener)
    }
    private val locationListeners = CopyOnWriteArrayList<LocationListener>()

    /**
     * Update location only if distance is >20m or time is >5 minutes
     */
    fun dispatchLocation(newLocation: Location) {
        var skip = false

        if (lastLocation != null) {
            val distance = newLocation.distanceTo(lastLocation!!)

            if (distance > 20 || newLocation.time - lastLocation!!.time > TimeHelper.FIVE_MINUTES_MS) {
                skip = false
            }
        }

        if (!skip) {
            for (locationListener in locationListeners) {
                locationListener.onLocationUpdate(newLocation)
            }
        }

        lastLocation = newLocation
    }
}