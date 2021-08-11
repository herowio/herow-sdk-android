package io.herow.sdk.detection.notification

import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.geofencing.GeofenceEvent
import java.util.concurrent.CopyOnWriteArrayList

object NotificationDispatcher {
    private val notificationListeners = CopyOnWriteArrayList<INotificationListener>()

    fun addNotificationListener(notificationListener: INotificationListener) {
        notificationListeners.add(notificationListener)
    }

    fun dispatchNotification(geofenceEvent: GeofenceEvent, campaign: Campaign, title: String, description: String) {
        GlobalLogger.shared.info(null, "Dispatching once notification is sent")
        GlobalLogger.shared.info(
            null,
            "Listeners in NotificationDispatcher: $notificationListeners"
        )
        for (listener in notificationListeners) {
            listener.onNotificationSent(geofenceEvent, campaign, title, description)
        }
    }
}