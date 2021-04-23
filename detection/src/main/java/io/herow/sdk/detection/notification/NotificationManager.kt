package io.herow.sdk.detection.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
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

    init {
        GeofenceDispatcher.addGeofenceListener(this)
    }

    fun registerFilter(campaign: Campaign) {
        filterList.add(Filter())
    }

    fun unregisterFilter(campaign: Campaign) {
        //TODO remove filter from filterList
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        var campaigns: List<Campaign>? = null
        if (geofenceEvents.isNotEmpty()) {
            for (event in geofenceEvents) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        campaigns = fetchCampaignInDatabase(event.zone)

                        if (campaigns != null && campaigns!!.isNotEmpty())
                            for (campaign in campaigns!!) {
                                if (campaign.trigger != null) {
                                    if ((event.type == GeofenceType.ENTER && !campaign.trigger!!.onExit) || (event.type == GeofenceType.EXIT && campaign.trigger!!.onExit)) {
                                        createNotification(event)
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


    private fun createNotification(event: GeofenceEvent) {
        val notifManager = NotificationHelper.setUpNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)

        val notificationId = NotificationHelper.hashCode(event.zone.hash + event.type)
        notifManager.notify(notificationId, builder.build())
        notified = true
    }
}