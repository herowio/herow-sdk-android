package io.herow.sdk.detection.notification

import android.content.Context
import androidx.room.Room
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
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.geofencing.GeofenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class NotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var db: HerowDatabase
    private lateinit var campaignRepository: CampaignRepository
    private lateinit var zoneRepository: ZoneRepository
    private lateinit var sessionHolder: SessionHolder
    private val listener = NotificationManagerListener()

    private val ioDispatcher = Dispatchers.IO
    private val liveEvents = arrayListOf<GeofenceEvent>()
    private val mockLocation = MockLocation()

    companion object {
        var campaignOne: Campaign? = null
    }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
        db = Room.databaseBuilder(context, HerowDatabase::class.java, "test").build()
        notificationManager = NotificationManager(context, sessionHolder)
        GeofenceDispatcher.addGeofenceListener(listener)

        campaignRepository = CampaignRepository(db.campaignDAO())
        zoneRepository = ZoneRepository(db.zoneDAO())
    }

    @Test
    fun testNotificationManager() {
        var zoneWithCampaigns: Zone?
        var zone: Zone?
        var campaignTwo: Campaign?

        runBlocking {
            Assert.assertTrue(listener.test == "NO")

            val job = async { MockDataInDatabase(context).createAndInsertZoneOne() }
            zone = job.await()

            val job2 = async { MockDataInDatabase(context).createAndInsertCampaignTwo() }
            campaignTwo = job2.await()

            val job3 = async(ioDispatcher) { zoneRepository.getZoneByHash(zone!!.hash) }
            zoneWithCampaigns = job3.await()

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
            Assert.assertTrue(zoneWithCampaigns!!.campaigns!!.size == 1)
            Assert.assertFalse(zoneWithCampaigns!!.campaigns!![0] == campaignTwo!!.id)
            Assert.assertFalse(listener.test == "OKAY")

            val job4 = async { MockDataInDatabase(context).createAndInsertCampaignOne() }
            campaignOne = job4.await()

            val job5 = async(ioDispatcher) { zoneRepository.getZoneByHash(zone!!.hash) }
            zoneWithCampaigns = job5.await()

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

            Assert.assertTrue(zoneWithCampaigns!!.campaigns!!.size == 1)
            Assert.assertTrue(zoneWithCampaigns!!.campaigns!![0] == campaignOne!!.id)
            Assert.assertTrue(listener.test == "OKAY")
        }
    }

    @After
    fun cleanUp() {
        db.close()
    }
}

class NotificationManagerListener(var test: String = "NO") : GeofenceListener {
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