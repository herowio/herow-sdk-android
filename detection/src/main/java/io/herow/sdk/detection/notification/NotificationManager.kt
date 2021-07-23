package io.herow.sdk.detection.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.detection.R
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.geofencing.IGeofenceListener
import io.herow.sdk.detection.notification.filters.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationManager(
    private val context: Context,
    private val sessionHolder: SessionHolder,
    private val ioDispatcher: CoroutineDispatcher
) :
    IGeofenceListener, KoinComponent {

    private val filterList: ArrayList<INotificationFilter> = arrayListOf()
    private val zoneRepository: ZoneRepository by inject()
    private val campaignRepository: CampaignRepository by inject()
    private var notificationOnExactEntry = false

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

    private fun addFilter(filter: INotificationFilter) {
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
        val trigger = if (notificationOnExactEntry) {
            GeofenceType.ENTER
        } else {
            GeofenceType.GEOFENCE_NOTIFICATION_ENTER
        }

        GlobalLogger.shared.info(context, "GeofenceEvents received: $geofenceEvents")
        if (geofenceEvents.isNotEmpty()) {
            for (event in geofenceEvents) {
                GlobalLogger.shared.info(context, "Geofence type is: ${event.type}")
                GlobalLogger.shared.info(context, "GeofenceEvents trigger is: $trigger")
                if (event.type == trigger) {
                    GlobalLogger.shared.info(
                        context,
                        "GeofenceEvents selected trigger is: $trigger"
                    )
                    runBlocking {
                        withContext(ioDispatcher) {
                            val campaigns = fetchCampaignInDatabase(event.zone)
                            val zoneName =
                                if (event.zone.access?.name != null) event.zone.access?.name else "non name"
                            val campaignNames = campaigns.map { it.notification?.title }
                            GlobalLogger.shared.info(
                                context,
                                "zone name: $zoneName campagnsName: $campaignNames"
                            )


                            if (campaigns.isNotEmpty()) {
                                for (campaign in campaigns) {
                                    GlobalLogger.shared.info(context, "Campaign are: $campaign")
                                    if (canCreateNotification(campaign)) {
                                        createNotification(context, event, campaign)
                                    } else {
                                        GlobalLogger.shared.info(
                                            context,
                                            "Campaign: $campaign not displayed"
                                        )
                                    }
                                }
                            } else {
                                GlobalLogger.shared.info(context, "no Campaign")
                            }
                        }
                    }
                }
            }
        } else {
            GlobalLogger.shared.info(context, "no Campaign")
        }
    }

    private fun fetchCampaignInDatabase(zone: Zone): List<Campaign> {
        val campaigns: MutableList<Campaign> = mutableListOf()
        val zoneCampaigns: List<String>? = zoneRepository.getZoneByHash(zone.hash)?.campaigns

        zoneCampaigns?.run {
            for (id in this) {
                campaignRepository.getCampaignByID(id)?.run {
                    campaigns.add(this)
                }
            }
        }

        return campaigns
    }


    private fun createNotification(context: Context, event: GeofenceEvent, campaign: Campaign) {
        GlobalLogger.shared.info(context, "Creating notification for $campaign")
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
        GlobalLogger.shared.info(context, "Creating notification for $title $description")
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.icon_notification)
            .setAutoCancel(true)
            .apply {
                setContentIntent(notificationPendingIntent)
            }

        with(notifManager) {
            notify(
                NotificationHelper.hashCode(event.zone.hash + event.type + campaign.campaignID),
                builder.build()
            )
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

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            NOTIFICATION_REQUEST_CODE,
            intent,
            pendingIntent
        )
    }

    fun notificationsOnExactZoneEntry(value: Boolean) {
        notificationOnExactEntry = value
        GlobalLogger.shared.info(
            context,
            "NotificationManager  exact entry: $notificationOnExactEntry"
        )

    }
}