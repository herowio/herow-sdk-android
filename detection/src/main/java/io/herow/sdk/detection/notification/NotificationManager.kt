package io.herow.sdk.detection.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
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

class NotificationManager(private val context: Context, private val sessionHolder: SessionHolder) :
    GeofenceListener {

    private val filterList: ArrayList<Filter> = arrayListOf()
    private val zoneRepository = ZoneRepository(HerowDatabase.getDatabase(context).zoneDAO())
    private val campaignRepository =
        CampaignRepository(HerowDatabase.getDatabase(context).campaignDAO())

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 2000
        const val ID_ZONE: String = "idZone"
        const val ID_CAMPAIGN: String = "idCampaign"
    }

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
                                    if (NotificationHelper.canCreateNotification(campaign, sessionHolder)) {
                                        NotificationDispatcher.dispatchNotification(event)
                                        createNotification(context, event, campaign)
                                    }
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


    private fun createNotification(context: Context, event: GeofenceEvent, campaign: Campaign) {
        GlobalLogger.shared.info(context, "Creating notification for $event")
        val notifManager = NotificationHelper.setUpNotificationChannel(context)

        val notificationPendingIntent =
            createNotificationPendingIntent(context, event.zone.hash, campaign.id!!)

        val title = NotificationHelper.computeDynamicContent(
            campaign.notification!!.title!!,
            event.zone,
            sessionHolder
        )

        val description = NotificationHelper.computeDynamicContent(
            campaign.notification!!.description!!,
            event.zone,
            sessionHolder
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.icon_notification)
            .apply {
                setContentIntent(notificationPendingIntent)
            }

        with(notifManager) {
            notify(NotificationHelper.hashCode(event.zone.hash + event.type), builder.build())
        }

        GlobalLogger.shared.info(context, "Dispatching notification for $event")
    }

    private fun createNotificationPendingIntent(
        context: Context,
        hash: String,
        idCampaign: String
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra(ID_ZONE, hash)
        intent.putExtra(ID_CAMPAIGN, idCampaign)

        return PendingIntent.getBroadcast(
            context,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}