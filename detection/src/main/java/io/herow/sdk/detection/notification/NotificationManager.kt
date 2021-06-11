package io.herow.sdk.detection.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.database.HerowDatabaseHelper
import io.herow.sdk.detection.R
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.notification.filters.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NotificationManager(private val context: Context, private val sessionHolder: SessionHolder) :
    GeofenceListener {

    private val filterList: ArrayList<NotificationFilter> = arrayListOf()
    private val zoneRepository = HerowDatabaseHelper.getZoneRepository(context)
    private val campaignRepository = HerowDatabaseHelper.getCampaignRepository(context)

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 2000
        const val ID_ZONE: String = "idZone"
        const val ID_CAMPAIGN: String = "idCampaign"
    }

    init {
        addFilter(ValidityFilter)
        addFilter(TimeSlotFilter)
        addFilter(DayRecurrencyFilter)
        addFilter(CappingFilter)
    }

    private fun addFilter(filter: NotificationFilter) {
        if (!filterList.contains(filter)) filterList.add(filter)
        println("Filter list is $filterList")
    }

    private fun canCreateNotification(campaign: Campaign): Boolean {
        for (filter in filterList) {
            if (!filter.createNotification(campaign, sessionHolder)) {
                return false
            }
        }

        return true
    }

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
                                    if (canCreateNotification(campaign)) {
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
            .setAutoCancel(true)
            .apply {
                setContentIntent(notificationPendingIntent)
            }

        with(notifManager) {
            notify(NotificationHelper.hashCode(event.zone.hash + event.type), builder.build())
            NotificationDispatcher.dispatchNotification(event, campaign)
        }

        GlobalLogger.shared.info(context, "Dispatching notification for $event")
    }

    @SuppressLint("InlinedApi")
    private fun createNotificationPendingIntent(
        context: Context,
        hash: String,
        idCampaign: String
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra(ID_ZONE, hash)
        intent.putExtra(ID_CAMPAIGN, idCampaign)

        val pendingIntent = if (DeviceHelper.getCurrentAndroidVersion(context) < 30) {
            PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getBroadcast(
            context,
            NOTIFICATION_REQUEST_CODE,
            intent,
            pendingIntent
        )
    }
}