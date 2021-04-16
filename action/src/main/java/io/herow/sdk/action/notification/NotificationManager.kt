package io.herow.sdk.action.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import io.herow.sdk.action.notification.model.Filter
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener

class NotificationManager(private val context: Context) : GeofenceListener {

    private val filterList: ArrayList<Filter> = arrayListOf()

    fun registerFilter(campaign: Campaign) {
        filterList.add(Filter())
    }

    fun unregisterFilter(campaign: Campaign) {
        //TODO remove filter
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        if (geofenceEvents.isNotEmpty()) {
            for (event in geofenceEvents) {
                if (event.zone.campaigns!!.isNotEmpty())
                    for (campaign in event.zone.campaigns!!) {
                        createNotification(event, campaign)
                    }
            }
        }
    }

    private fun createNotification(event: GeofenceEvent, campaign: String) {
        val notifManager = NotificationHelper.setUpNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)

        val notificationId = NotificationHelper.hashCode(event.zone.hash + event.type)
        notifManager.notify(notificationId, builder.build())
    }
}