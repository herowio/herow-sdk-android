package io.herow.sdk.detection.notification

import io.herow.sdk.detection.geofencing.GeofenceEvent

interface NotificationListener {
    fun onNotificationSent(geofenceEvent: GeofenceEvent)
}