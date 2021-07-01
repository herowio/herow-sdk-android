package io.herow.sdk.detection

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowInitializerTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private var campaigns: List<Campaign>? = mutableListOf()
    private var zones: List<Zone>? = mutableListOf()
    private var pois: List<Poi>? = mutableListOf()
    private lateinit var database: HerowDatabase
    private lateinit var zoneRepository: ZoneRepository
    private lateinit var poiRepository: PoiRepository
    private lateinit var campaignRepository: CampaignRepository

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))

        database = Room.databaseBuilder(context, HerowDatabase::class.java, "testReset").build()
        zoneRepository = ZoneRepository(database.zoneDAO())
        poiRepository = PoiRepository(database.poiDAO())
        campaignRepository = CampaignRepository(database.campaignDAO())
    }

    @Test
    fun testHerowInitializer() {
        sessionHolder.reset()

        Assert.assertTrue(sessionHolder.getAll() == 0)

        runBlocking {
            withContext(Dispatchers.IO) {
                pois = poiRepository.getAllPois()
                zones = zoneRepository.getAllZones()
                campaigns = campaignRepository.getAllCampaigns()
            }
        }

        Assert.assertTrue(pois.isNullOrEmpty())
        Assert.assertTrue(zones.isNullOrEmpty())
        Assert.assertTrue(campaigns.isNullOrEmpty())
    }
}