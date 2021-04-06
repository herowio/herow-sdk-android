package io.herow.sdk.detection.location

import android.location.Location
import android.util.Log
import io.herow.sdk.common.helpers.TimeHelper
import java.util.concurrent.CopyOnWriteArrayList

object LocationDispatcher {

    private var lastLocation: Location? = null
    private var countdown = 2

    fun addLocationListener(locationListener: LocationListener) {
        locationListeners.add(locationListener)
    }

    private val locationListeners = CopyOnWriteArrayList<LocationListener>()

    /**
     * Update location only if distance is >20m or if distance is <20 & time is >5 minutes
     */
    fun dispatchLocation(newLocation: Location) {
        var skip = false

        Log.i("XXX/EVENT", "LocationDispatcher - Value of skip at beginning: $skip")

        if (lastLocation != null) {

            Log.i("XXX/EVENT", "LocationDispatcher - LastLocation is: $lastLocation")
            Log.i("XXX/EVENT", "LocationDispatcher - NewLocation is: $newLocation")

            if (lastLocation!!.latitude != newLocation.latitude && lastLocation!!.longitude != newLocation.longitude) {
                Log.i("XXX/EVENT", "LocationDispatcher - New and Last locations are different")
                val distance = newLocation.distanceTo(lastLocation!!)


                Log.i("XXX/EVENT", "LocationDispatcher - Distance is: $distance")
                val time = newLocation.time - lastLocation!!.time
                val timeInSeconds = time / 1000

                Log.i("XXX/EVENT", "LocationDispatcher - Time is: $timeInSeconds")

                skip =
                    distance < 10 && newLocation.time - lastLocation!!.time < TimeHelper.THREE_MINUTE_MS
            } else {
                skip = true
            }
        }

        Log.i("XXX/EVENT", "LocationDispatcher - Value of skip: $skip")

        if (!skip) {
            for (locationListener in locationListeners) {
                Log.i(
                    "XXX/EVENT",
                    "LocationDispatcher - Calling onLocationUpdated: $locationListener"
                )

                locationListener.onLocationUpdate(newLocation)
            }

            lastLocation = newLocation
        }

        Log.i("XXX/EVENT", "LocationDispatcher - End value of skip: $skip")
    }
}