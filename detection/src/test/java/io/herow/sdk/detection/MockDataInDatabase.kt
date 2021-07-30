package io.herow.sdk.detection

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Capping
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.test.KoinTest
import org.koin.test.inject

class MockDataInDatabase : KoinTest {

    private val database: HerowDatabase by inject()
    private val zoneRepository: ZoneRepository by inject()
    private val campaignRepository: CampaignRepository by inject()

    suspend fun createAndInsertZoneOne(dispatcher: CoroutineDispatcher): Zone {
        val zone = Zone(
            hash = "testHashNotification",
            lat = 48.117266,
            lng = -1.6777926,
            radius = 2.0,
            campaigns = listOf("campaignOne"),
            access = null
        )

        var zoneInDB: Zone?

        withContext(dispatcher) {
            zoneRepository.insert(zone)
            zoneInDB = zoneRepository.getZoneByHash(zone.hash)!!
        }

        return zoneInDB!!
    }

    suspend fun createAndInsertCampaignOne(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "campaignOne"
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createAndInsertCampaignTwo(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(id = "campaignTwo")
        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun updateCampaignTwoWithCapping(dispatcher: CoroutineDispatcher): Campaign {
        val capping = Capping(
            maxNumberNotifications = 5
        )
        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignInDB = campaignRepository.getCampaignByID("campaignTwo")
            campaignInDB!!.capping = capping
            campaignRepository.update(campaignInDB!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithNoEnd(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignNoEnd",
            begin = TimeHelper.getCurrentTime() - 5000
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun updateCampaignWithEndBefore(campaign: Campaign, dispatcher: CoroutineDispatcher): Campaign {
        var campaignInDb: Campaign?

        withContext(dispatcher) {
            campaign.end = TimeHelper.getCurrentTime() - 3000
            campaignRepository.update(campaign)
            campaignInDb = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDb!!
    }

    suspend fun updateCampaignWithEndAfter(campaign: Campaign, dispatcher: CoroutineDispatcher): Campaign {
        var campaignInDb: Campaign?

        withContext(dispatcher) {
            campaign.end = TimeHelper.getCurrentTime() + 3000
            campaignRepository.update(campaign)
            campaignInDb = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDb!!
    }

    suspend fun createCampaignWithLateBegin(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithLateBegin",
            begin = 1780264800000
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithMondayTuesdayFriday(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithMondayTuesdayAndFriday",
            daysRecurrence = listOf("monday", "tuesday", "friday")
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithWednesday(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithWednesday",
            daysRecurrence = listOf("tuesday", "wednesday")
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithOnlyStartHour(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithOnlyStartHour",
            startHour = "02:00"
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithOnlyStopHour(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithOnlyStopHour",
            stopHour = "23:00"
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithShortSlot(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithShortSlot",
            startHour = "01:00",
            stopHour = "02:00"
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithLongSlot(dispatcher: CoroutineDispatcher): Campaign {
        val campaign = Campaign(
            id = "CampaignWithLongSlot",
            startHour = "02:00",
            stopHour = "23:00"
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }

    suspend fun createCampaignWithCapping(dispatcher: CoroutineDispatcher): Campaign {
        val capping = Capping(
            maxNumberNotifications = 5
        )
        val campaign = Campaign(
            id = "CampaignWithCapping",
            capping = capping
        )

        var campaignInDB: Campaign?

        withContext(dispatcher) {
            campaignRepository.insert(campaign)
            campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
        }

        return campaignInDB!!
    }
}