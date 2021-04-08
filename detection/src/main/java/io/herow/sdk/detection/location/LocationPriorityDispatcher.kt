package io.herow.sdk.detection.location
import com.google.android.gms.location.LocationRequest
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object LocationPriorityDispatcher {

    private var currentPriority = LocationPriority.VERY_LOW
    private val locationPriorityListeners = CopyOnWriteArrayList<LocationPriorityListener>()

    fun registerLocationPriorityListener(listener: LocationPriorityListener) {
        GlobalLogger.shared.debug(null, "register  priority listener  : $listener")
        locationPriorityListeners.add(listener)
    }

    fun unregisterLocationPriorityListener(listener: LocationPriorityListener) {
        locationPriorityListeners.remove(listener)
    }

    private fun dispatchPriority(priority: LocationPriority) {
        if (priority != currentPriority) {
            currentPriority = priority
            GlobalLogger.shared.debug(null, "dispatch location priority : $priority")
            locationPriorityListeners.forEach {
                it.onLocationPriority(priority)
            }
        } else {
            GlobalLogger.shared.debug(null, "priority is the same no need to dispatch : $priority")
        }
    }

    fun dispatchPriorityForDistance( distance: Double) {
        if (distance < 3000.0) {
            dispatchPriority(LocationPriority.VERY_LOW)
        }
        if (distance < 1000.0) {
            dispatchPriority(LocationPriority.LOW)
        }
        if (distance < 500) {
            dispatchPriority(LocationPriority.MEDIUM)
        }
        if (distance < 100.0) {
            dispatchPriority(LocationPriority.HIGHT)
        }
        if (distance < 50) {
            dispatchPriority(LocationPriority.VERY_HIGHT)
        }
    }
}