package io.herow.sdk.detection.location
import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object LocationPriorityDispatcher {

    private var currentPriority = LocationPriority.VERY_HIGH
    private  var onForeGround = false
    private val locationPriorityListeners = CopyOnWriteArrayList<LocationPriorityListener>()

    fun setOnForeGround(value: Boolean) {
        onForeGround = value
        if (onForeGround) {
            dispatchPriority(LocationPriority.VERY_HIGH)
        }
    }

    fun registerLocationPriorityListener(listener: LocationPriorityListener) {
        GlobalLogger.shared.debug(null, "register  priority listener  : $listener")
        locationPriorityListeners.add(listener)
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
        if (onForeGround) {
            dispatchPriority(LocationPriority.VERY_HIGH)
            return
        }
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
            dispatchPriority(LocationPriority.HIGH)
        }
        if (distance < 50) {
            dispatchPriority(LocationPriority.VERY_HIGH)
        }
    }

}