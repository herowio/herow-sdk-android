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

            "Value of skip at beginning: $skip"
        )

        if (lastLocation != null) {
            GlobalLogger.shared.info(
                null,
                "LastLocation is: $lastLocation"
            )
            GlobalLogger.shared.info(
                null,

                "NewLocation is: $newLocation"
            )

            if (lastLocation!!.latitude != newLocation.latitude && lastLocation!!.longitude != newLocation.longitude) {
                GlobalLogger.shared.info(
                    null,

                    "New and Last locations are different"
                )
                val distance = newLocation.distanceTo(lastLocation!!)

                GlobalLogger.shared.info(
                    null,

                    "Distance is: $distance"
                )
                val time = newLocation.time - lastLocation!!.time
                val timeInSeconds = time / 1000
                GlobalLogger.shared.info(
                    null,

                    "Time is: $timeInSeconds"
                )

                skip =
                    distance < 10 && newLocation.time - lastLocation!!.time < TimeHelper.THREE_MINUTE_MS
            } else {
                skip = true
            }
        }
        GlobalLogger.shared.info(
            null,

            "Value of skip: $skip"
        )

        if (!skip) {
            for (locationListener in locationListeners) {
                GlobalLogger.shared.info(
                    null,

                    "Dispatching location to: $locationListener"
                )
                locationListener.onLocationUpdate(newLocation)
            }

            lastLocation = newLocation
        }
        GlobalLogger.shared.info(
            null,

            "End value of skip: $skip"
        )
    }
}