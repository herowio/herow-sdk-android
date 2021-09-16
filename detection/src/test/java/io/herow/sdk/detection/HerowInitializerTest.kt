package io.herow.sdk.detection

import android.content.Context
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
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.*
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

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private var campaigns: List<Campaign>? = mutableListOf()
    private var zones: List<Zone>? = mutableListOf()
    private var pois: List<Poi>? = mutableListOf()

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val database: HerowDatabase by inject()
    private val zoneRepository: ZoneRepository by inject()
    private val poiRepository: PoiRepository by inject()
    private val campaignRepository: CampaignRepository by inject()

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
    }

    @Test
    fun testHerowInitializer() = runBlocking {
        sessionHolder.reset()

        Assert.assertTrue(sessionHolder.getAll() == 0)

        CoroutineScope(ioDispatcher).launch(CoroutineName("test-herow-initializer")) {
            pois = poiRepository.getAllPois()
            zones = zoneRepository.getAllZones()
            campaigns = campaignRepository.getAllCampaigns()
        }

        Assert.assertTrue(pois.isNullOrEmpty())
        Assert.assertTrue(zones.isNullOrEmpty())
        Assert.assertTrue(campaigns.isNullOrEmpty())
    }
}