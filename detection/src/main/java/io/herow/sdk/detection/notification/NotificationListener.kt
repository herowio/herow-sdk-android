package io.herow.sdk.detection.notification

import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.geofencing.GeofenceEvent

interface NotificationListener {
    fun onNotificationSent(geofenceEvent: GeofenceEvent, campaign: Campaign)
}