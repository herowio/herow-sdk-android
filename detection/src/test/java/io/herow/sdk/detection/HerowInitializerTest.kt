package io.herow.sdk.detection

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowInitializerTest : KoinTest {

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
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(databaseModuleTest, dispatcherModule)
        }

        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
    }

    @Test
    fun testHerowInitializer() = runBlocking {
        sessionHolder.reset()

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
        stopKoin()
    }
}