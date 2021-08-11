package io.herow.sdk.detection.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.koin.databaseModuleTest
import io.herow.sdk.detection.koin.dispatcherModule
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.geofencing.IGeofenceListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class NotificationManagerTest : AutoCloseKoinTest() {

    private val ioDispatcher: CoroutineDispatcher by inject()
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    private val db: HerowDatabase by inject()
    private val campaignRepository: CampaignRepository by inject()
    private val zoneRepository: ZoneRepository by inject()

    private lateinit var sessionHolder: SessionHolder
    private val listener = NotificationManagerListener()
    private val liveEvents = arrayListOf<GeofenceEvent>()
    private val mockLocation = MockLocation()

    companion object {
        var campaignOne: Campaign? = null
    }

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            allowOverride(true)
            androidContext(ApplicationProvider.getApplicationContext())
            modules(databaseModuleTest, dispatcherModule)
        }

        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
        notificationManager = NotificationManager(context, sessionHolder)
        GeofenceDispatcher.addGeofenceListener(listener)
    }

    @Test
    fun testNotificationManager() = runBlocking {
            var zoneWithCampaigns: Zone?
            var zone: Zone?
            var campaignTwo: Campaign?

            Assert.assertTrue(listener.test == "NO")

            withContext(ioDispatcher) {
                zone = MockDataInDatabase().createAndInsertZoneOne(ioDispatcher)
            }

            withContext(ioDispatcher) {
                campaignTwo =
                    MockDataInDatabase().createAndInsertCampaignTwo(ioDispatcher)
            }

            withContext(ioDispatcher) {
                zoneWithCampaigns = zoneRepository.getZoneByHash(zone?.hash!!)
            }

            // The ID of the CampaignTwo is not available into the list of id's campaigns of ZoneOne
            liveEvents.add(
                GeofenceEvent(
                    zoneWithCampaigns as Zone,
                    mockLocation.buildLocation(),
                    GeofenceType.GEOFENCE_NOTIFICATION_ENTER,
                    0.0
                )
            )

            GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)

            Assert.assertTrue(zoneWithCampaigns?.campaigns!!.size == 1)
            Assert.assertFalse(zoneWithCampaigns?.campaigns!![0] == campaignTwo?.id)
            Assert.assertFalse(listener.test == "OKAY")

            withContext(ioDispatcher) {
                campaignOne =
                    MockDataInDatabase().createAndInsertCampaignOne(ioDispatcher)
            }

            withContext(ioDispatcher) {
                zoneWithCampaigns = zoneRepository.getZoneByHash(zone?.hash!!)
            }

            // The ID of the CampaignOne is available into the list of id's campaigns of ZoneOne
            liveEvents.clear()
            liveEvents.add(
                GeofenceEvent(
                    zoneWithCampaigns as Zone,
                    mockLocation.buildLocation(),
                    GeofenceType.GEOFENCE_NOTIFICATION_ENTER,
                    0.0
                )
            )

            GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)

            Assert.assertTrue(zoneWithCampaigns?.campaigns!!.size == 1)
            Assert.assertTrue(zoneWithCampaigns?.campaigns!![0] == campaignOne!!.id)
            Assert.assertTrue(listener.test == "OKAY")
    }
}

@ExperimentalCoroutinesApi
class NotificationManagerListener(var test: String = "NO") : IGeofenceListener {

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        for (event in geofenceEvents) {
            if (event.type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
                val campaignToTest: Campaign? = NotificationManagerTest.campaignOne
                val campaignFromEvent = event.zone.campaigns!![0]

                if (NotificationManagerTest.campaignOne != null && campaignToTest!!.id == campaignFromEvent)
                    test = "OKAY"
            }
        }
    }
}