package io.herow.sdk.detection

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Capping
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

class MockDataInDatabase : ICustomKoinTestComponent {
    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val db: HerowDatabase = Room.databaseBuilder(
        context,
        HerowDatabase::class.java,
        "herow_test_BDD"
    ).build()

    private val zoneRepository: ZoneRepository = ZoneRepository(db.zoneDAO())
    private val campaignRepository: CampaignRepository = CampaignRepository(db.campaignDAO())

    private val ioDispatcher: CoroutineDispatcher by inject()

    init {
        HerowKoinTestContext.init(context)
    }

    fun createAndInsertZoneOne(): Zone {
        val zone = Zone(
            hash = "testHashNotification",
            lat = 48.117266,
            lng = -1.6777926,
            radius = 2.0,
            campaigns = listOf("campaignOne"),
            access = null
        )

        val zoneByHash = runBlocking {
            withContext(ioDispatcher) {
                zoneRepository.insert(zone)
                zoneRepository.getZoneByHash(zone.hash)
            }
        }

        db.close()
        return zoneByHash!!
    }

    fun createAndInsertCampaignOne(): Campaign {
        val campaign = Campaign(
            id = "campaignOne"
        )

        val campaignById = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignById!!
    }

    fun createAndInsertCampaignTwo(): Campaign {
        val campaign = Campaign(id = "campaignTwo")

        campaignRepository.insert(campaign)
        val campaignInDb = campaignRepository.getCampaignByID(campaign.id!!)
        db.close()
        return campaignInDb!!
    }

    fun updateCampaignTwoWithCapping(): Campaign {
        val capping = Capping(
            maxNumberNotifications = 5
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                val tmpCampaign = campaignRepository.getCampaignByID("campaignTwo")
                tmpCampaign!!.capping = capping
                campaignRepository.update(tmpCampaign)
                tmpCampaign
            }
        }
        db.close()
        return campaignInDb
    }

    fun createCampaignWithNoEnd(): Campaign {
        val campaign = Campaign(
            id = "CampaignNoEnd",
            begin = TimeHelper.getCurrentTime() - 5000
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun updateCampaignWithEndBefore(campaign: Campaign): Campaign {
        campaign.end = TimeHelper.getCurrentTime() - 3000

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.update(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun updateCampaignWithEndAfter(campaign: Campaign): Campaign {
        campaign.end = TimeHelper.getCurrentTime() + 3000

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.update(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithLateBegin(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithLateBegin",
            begin = 1780264800000
        )

        campaignRepository.insert(campaign)
        val campaignInDb = campaignRepository.getCampaignByID(campaign.id!!)
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithMondayTuesdayFriday(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithMondayTuesdayAndFriday",
            daysRecurrence = listOf("monday", "tuesday", "friday")
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithWednesday(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithWednesday",
            daysRecurrence = listOf("tuesday", "wednesday")
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithOnlyStartHour(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithOnlyStartHour",
            startHour = "02:00"
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }


    fun createCampaignWithOnlyStopHour(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithOnlyStopHour",
            stopHour = "23:00"
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithShortSlot(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithShortSlot",
            startHour = "01:00",
            stopHour = "02:00"
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithLongSlot(): Campaign {
        val campaign = Campaign(
            id = "CampaignWithLongSlot",
            startHour = "02:00",
            stopHour = "23:00"
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }

    fun createCampaignWithCapping(): Campaign {
        val capping = Capping(
            maxNumberNotifications = 5
        )
        val campaign = Campaign(
            id = "CampaignWithCapping",
            capping = capping
        )

        val campaignInDb = runBlocking {
            withContext(ioDispatcher) {
                campaignRepository.insert(campaign)
                campaignRepository.getCampaignByID(campaign.id!!)
            }
        }
        db.close()
        return campaignInDb!!
    }
}