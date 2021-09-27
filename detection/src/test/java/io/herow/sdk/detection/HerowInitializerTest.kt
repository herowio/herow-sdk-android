package io.herow.sdk.detection

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowInitializerTest : KoinTest, ICustomKoinTestComponent {

    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sessionHolder: SessionHolder by inject()
    private var campaigns: List<Campaign>? = mutableListOf()
    private var zones: List<Zone>? = mutableListOf()
    private var pois: List<Poi>? = mutableListOf()

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val database: HerowDatabase =
        Room.databaseBuilder(context, HerowDatabase::class.java, "herow_test_BDD").build()
    private val zoneRepository: ZoneRepository = ZoneRepository(database.zoneDAO())
    private val poiRepository: PoiRepository = PoiRepository(database.poiDAO())
    private val campaignRepository: CampaignRepository = CampaignRepository(database.campaignDAO())

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()
    }

    @Test
    fun testHerowInitializer() = runBlocking {
        Assert.assertTrue(sessionHolder.getAll() == 0)

        withContext(ioDispatcher) {
            pois = poiRepository.getAllPois()
            zones = zoneRepository.getAllZones()
            campaigns = campaignRepository.getAllCampaigns()
        }

        Assert.assertTrue(pois.isNullOrEmpty())
        Assert.assertTrue(zones.isNullOrEmpty())
        Assert.assertTrue(campaigns.isNullOrEmpty())
    }

    @After
    fun cleanUp() {
        database.close()
    }
}