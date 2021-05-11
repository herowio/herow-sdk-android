package io.herow.sdk.detection

import android.content.Context
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MockDataInDatabase(context: Context) {

    private val db: HerowDatabase = HerowDatabase.getDatabase(context)
    private val zoneRepository: ZoneRepository = ZoneRepository(db.zoneDAO())
    private val campaignRepository: CampaignRepository = CampaignRepository(db.campaignDAO())

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
        val job =  CoroutineScope(Dispatchers.IO).async {
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
            id = "campaignTWo"
        )

        var campaignInDB: Campaign? = null
        val job =  CoroutineScope(Dispatchers.IO).async {
            withContext(Dispatchers.IO) {
                campaignRepository.insert(campaign)
                campaignInDB = campaignRepository.getCampaignByID(campaign.id!!)
            }
        }

        job.await()
        return campaignInDB!!
    }
}