package io.herow.sdk.detection.notification

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.database.HerowDatabase
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.geofencing.IGeofenceListener
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
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class NotificationManagerTest : AutoCloseKoinTest(), ICustomKoinTestComponent {
    private val ioDispatcher: CoroutineDispatcher by inject()
    private val sessionHolder: SessionHolder by inject()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var notificationManager: NotificationManager

    private val db: HerowDatabase = Room.databaseBuilder(
        context,
        HerowDatabase::class.java,
        "herow_test_BDD"
    ).build()

    private val campaignRepository: CampaignRepository = CampaignRepository(db.campaignDAO())
    private val zoneRepository: ZoneRepository = ZoneRepository(db.zoneDAO())
    private val listener = NotificationManagerListener()
    private val liveEvents = arrayListOf<GeofenceEvent>()
    private val mockLocation = MockLocation()

    companion object {
        var campaignOne: Campaign? = null
    }

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()
        notificationManager = NotificationManager(context)

        GeofenceDispatcher.addGeofenceListener(listener)
    }

    @Test
    fun testNotificationManager(): Unit = runBlocking {
        withContext(ioDispatcher) {
            Assert.assertTrue(listener.test == "NO")

            val zone = MockDataInDatabase().createAndInsertZoneOne()
            val campaignTwo = MockDataInDatabase().createAndInsertCampaignTwo()
            var zoneWithCampaigns: Zone? = zoneRepository.getZoneByHash(zone.hash)

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

            Assert.assertTrue(zoneWithCampaigns.campaigns!!.size == 1)
            Assert.assertFalse(zoneWithCampaigns.campaigns!![0] == campaignTwo.id)
            Assert.assertFalse(listener.test == "OKAY")

            campaignOne = MockDataInDatabase().createAndInsertCampaignOne()
            zoneWithCampaigns = zoneRepository.getZoneByHash(zone.hash)

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

            Assert.assertTrue(zoneWithCampaigns.campaigns!!.size == 1)
            Assert.assertTrue(zoneWithCampaigns.campaigns!![0] == campaignOne!!.id)
            Assert.assertTrue(listener.test == "OKAY")
        }
    }

    @After
    fun cleanup() {
        db.close()
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