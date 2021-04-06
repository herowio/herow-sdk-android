package io.herow.sdk.detection.location

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object LocationDispatcher {

    private var lastLocation: Location? = null

    fun addLocationListener(locationListener: LocationListener) {
        locationListeners.add(locationListener)
    }

    private val locationListeners = CopyOnWriteArrayList<LocationListener>()

    /**
     * Update location only if distance is >20m or if distance is <20 & time is >5 minutes
     */
    fun dispatchLocation(newLocation: Location) {
        var skip = false
        GlobalLogger.shared.info(
            null,
            "LocationDispatcher",
            "dispatchLocation",
            24,
            "Value of skip at beginning: $skip"
        )

        if (lastLocation != null) {
            GlobalLogger.shared.info(
                null,
                "LocationDispatcher",
                "dispatchLocation",
                27,
                "LastLocation is: $lastLocation"
            )
            GlobalLogger.shared.info(
                null,
                "LocationDispatcher",
                "dispatchLocation",
                28,
                "NewLocation is: $newLocation"
            )

            if (lastLocation!!.latitude != newLocation.latitude && lastLocation!!.longitude != newLocation.longitude) {
                GlobalLogger.shared.info(
                    null,
                    "LocationDispatcher",
                    "dispatchLocation",
                    31,
                    "New and Last locations are different"
                )
                val distance = newLocation.distanceTo(lastLocation!!)

                GlobalLogger.shared.info(
                    null,
                    "LocationDispatcher",
                    "dispatchLocation",
                    34,
                    "Distance is: $distance"
                )
                val time = newLocation.time - lastLocation!!.time
                val timeInSeconds = time / 1000
                GlobalLogger.shared.info(
                    null,
                    "LocationDispatcher",
                    "dispatchLocation",
                    37,
                    "Time is: $timeInSeconds"
                )

                skip =
                    distance < 20 && newLocation.time - lastLocation!!.time < TimeHelper.FIVE_MINUTES_MS
            }
        }
        GlobalLogger.shared.info(
            null,
            "LocationDispatcher",
            "dispatchLocation",
            43,
            "Value of skip: $skip"
        )

        if (!skip) {
            for (locationListener in locationListeners) {
                GlobalLogger.shared.info(
                    null,
                    "LocationDispatcher",
                    "dispatchLocation",
                    47,
                    "Dispatching location to: $locationListener"
                )
                locationListener.onLocationUpdate(newLocation)
            }

            lastLocation = newLocation
        }
        GlobalLogger.shared.info(
            null,
            "LocationDispatcher",
            "dispatchLocation",
            53,
            "End value of skip: $skip"
        )
    }
}