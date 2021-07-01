package io.herow.sdk.detection

import android.content.Context
import androidx.room.Room
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Capping
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MockDataInDatabase(context: Context) {
    private val database: HerowDatabase = Room.databaseBuilder(context, HerowDatabase::class.java, "test").build()
    private val zoneRepository: ZoneRepository = ZoneRepository(database.zoneDAO())
    private val campaignRepository: CampaignRepository = CampaignRepository(database.campaignDAO())

    suspend fun createAndInsertZoneOne(): Zone {
        val zone = Zone(
            hash = "testHashNotification",
            lat = 48.117266,
            lng = -1.6777926,
            radius = 2.0,
            campaigns = listOf("campaignOne"),
            access = null
        )

        var zoneInDB: Zone? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            zoneRepository.insert(zone)
            zoneInDB = zoneRepository.getZoneByHash(zone.hash)!!
        }

        job.await()
        return zoneInDB!!
    }

    suspend fun createAndInsertCampaignOne(): Campaign {
        val campaign = Campaign(
            id = "campaignOne"
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createAndInsertCampaignTwo(): Campaign {
        val campaign = Campaign(
            id = "campaignTwo"
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }
    
    suspend fun updateCampaignTwoWithCapping(): Campaign {
        val capping = Capping(
            maxNumberNotifications = 5
        )
        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignInDB = campaignRepository.getCampaignByID("campaignTwo")
                campaignInDB!!.capping = capping
                campaignRepository.update(campaignInDB!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithNoEnd(): Campaign {
        val campaign = Campaign(
            id = "CampaignNoEnd",
            begin = TimeHelper.getCurrentTime() - 5000
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun updateCampaignWithEndBefore(campaign: Campaign): Campaign {
        var campaignInDb: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            campaign.end = TimeHelper.getCurrentTime() - 3000
            campaignRepository.update(campaign)
            campaignInDb = campaignRepository.getCampaignByID(campaign.id!!)
        }

        job.await()
        return campaignInDb!!
    }

    suspend fun updateCampaignWithEndAfter(campaign: Campaign): Campaign {
        var campaignInDb: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            campaign.end = TimeHelper.getCurrentTime() + 3000
            campaignRepository.update(campaign)
            campaignInDb = campaignRepository.getCampaignByID(campaign.id!!)
        }

        job.await()
        return campaignInDb!!
    }

    suspend fun createCampaignWithLateBegin(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithLateBegin",
            begin = 1780264800000
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithMondayTuesdayFriday(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithMondayTuesdayAndFriday",
            daysRecurrence = listOf("monday", "tuesday", "friday")
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithWednesday(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithWednesday",
            daysRecurrence = listOf("tuesday", "wednesday")
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithOnlyStartHour(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithOnlyStartHour",
            startHour = "02:00"
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithOnlyStopHour(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithOnlyStopHour",
            stopHour = "23:00"
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithShortSlot(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithShortSlot",
            startHour = "01:00",
            stopHour = "02:00"
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithLongSlot(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithLongSlot",
            startHour = "02:00",
            stopHour = "23:00"
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }

    suspend fun createCampaignWithCapping(): Campaign {
        val capping = Capping(
            maxNumberNotifications = 5
        )
        val campaign = Campaign(
            id = "CampaignWithCapping",
            capping = capping
        )

        var campaignInDB: Campaign? = null
        val job = CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }
}
