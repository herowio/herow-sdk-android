package io.herow.sdk.detection.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.R
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.notification.model.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NotificationManager(private val context: Context) : GeofenceListener {

    private val filterList: ArrayList<Filter> = arrayListOf()
    private val zoneRepository = ZoneRepository(HerowDatabase.getDatabase(context).zoneDAO())
    private val campaignRepository =
        CampaignRepository(HerowDatabase.getDatabase(context).campaignDAO())
    var notified: Boolean = false

    /* fun registerFilter(campaign: Campaign) {
        //TODO create filter
        filterList.add(Filter())
    }

    fun unregisterFilter(campaign: Campaign) {
        //TODO remove filter from filterList
    } */

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        GlobalLogger.shared.info(context, "GeofenceEvents received: $geofenceEvents")
        if (geofenceEvents.isNotEmpty()) {
            for (event in geofenceEvents) {
                GlobalLogger.shared.info(context, "Geofence type is: ${event.type}")
                if (event.type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            val campaigns = fetchCampaignInDatabase(event.zone)

                            if (campaigns.isNotEmpty())
                                for (campaign in campaigns) {
                                    NotificationDispatcher.dispatchNotification(event)
                                    createNotification(event, campaign)
                                }
                        }
                    }
                }
            }
        }
    }

    private fun fetchCampaignInDatabase(zone: Zone): List<Campaign> {
        val campaigns: MutableList<Campaign> = mutableListOf()
        val zoneCampaigns = zoneRepository.getZoneByHash(zone.hash)!!.campaigns

        for (id in zoneCampaigns as List<String>) {
            campaignRepository.getCampaignByID(id)?.let {
                campaigns.add(it)
            }
        }

        return campaigns
    }


    private fun createNotification(event: GeofenceEvent, campaign: Campaign) {
        GlobalLogger.shared.info(context, "Creating notification for $event")
        val notifManager = NotificationHelper.setUpNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setContentTitle(campaign.notification?.title)
            .setContentText(campaign.notification?.description)
            .setSmallIcon(R.drawable.icon_notification)

        val notificationId = NotificationHelper.hashCode(event.zone.hash + event.type)
        notifManager.notify(notificationId, builder.build())

        notified = true
        GlobalLogger.shared.info(context, "Dispatching notification for $event")
    }
}