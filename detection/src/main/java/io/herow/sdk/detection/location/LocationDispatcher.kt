package io.herow.sdk.detection.location

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object LocationDispatcher {
    private var lastLocation: Location? = null
    private var skipCount = 0

    fun addLocationListener(locationListener: ILocationListener) {
        locationListeners.add(locationListener)
    }

    private val locationListeners = CopyOnWriteArrayList<ILocationListener>()

    /**
     * Update location only if distance is >20 m or if distance is <20 & time is >5 minutes
     */
    fun dispatchLocation(newLocation: Location) {
        var skip = false
        GlobalLogger.shared.debug(
            null,
            " try to dispatchLocation : $newLocation"
        )
        if (lastLocation != null) {
            GlobalLogger.shared.info(null, "LastLocation is: $lastLocation")
            GlobalLogger.shared.info(null, "NewLocation is: $newLocation")

            skip =
                if (lastLocation!!.latitude != newLocation.latitude && lastLocation!!.longitude != newLocation.longitude) {
                    GlobalLogger.shared.info(null, "New and Last locations are different")
                    val distance = newLocation.distanceTo(lastLocation!!)

                    GlobalLogger.shared.info(null, "Distance is: $distance")
                    val time = newLocation.time - lastLocation!!.time
                    val timeInSeconds = time / 1000
                    GlobalLogger.shared.info(null, "Time is: $timeInSeconds")

                    distance < 10 && newLocation.time - lastLocation!!.time < TimeHelper.FIVE_SECONDS_MS
                } else {
                    true
                }
        }

        GlobalLogger.shared.info(null, "Value of skip: $skip")

        if (!skip || skipCount > 5) {
            skipCount = 0
            for (locationListener in locationListeners) {
                GlobalLogger.shared.info(null, "Dispatching location to: $locationListener")
                locationListener.onLocationUpdate(newLocation)
            }

            lastLocation = newLocation
        } else {
            skipCount += 1
        }
        GlobalLogger.shared.info(null, "End value of skip: $skip")
    }
}