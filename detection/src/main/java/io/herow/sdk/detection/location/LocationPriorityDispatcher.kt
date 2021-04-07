package io.herow.sdk.detection.location
import java.util.concurrent.CopyOnWriteArrayList

object LocationPriorityDisptacher {

    private val locationPriorityListeners = CopyOnWriteArrayList<LocationPriorityListener>()

    fun registerLocationPriorityListener(listener: LocationPriorityListener) {
        LocationPriorityDisptacher.locationPriorityListeners.add(listener)
    }

    fun unregisterLocationPriorityListener(listener: LocationPriorityListener) {
        LocationPriorityDisptacher.locationPriorityListeners.remove(listener)
    }
}