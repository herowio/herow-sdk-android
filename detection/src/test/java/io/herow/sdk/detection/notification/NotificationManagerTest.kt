package io.herow.sdk.detection.notification

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
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
    private val listener = NotificationManagerListener()

    private val ioDispatcher = Dispatchers.IO
    private val liveEvents = arrayListOf<GeofenceEvent>()

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        notificationManager = NotificationManager(context)
        db = HerowDatabase.getDatabase(context)
        val campaignDAO = db.campaignDAO()
        val zoneDAO = db.zoneDAO()
        GeofenceDispatcher.addGeofenceListener(listener)

        campaignRepository = CampaignRepository(campaignDAO)
        zoneRepository = ZoneRepository(zoneDAO)
    }

    @Test
    fun testNotificationManager() {
        var zoneWithCampaigns: Zone?
        var zone: Zone?
        var campaignOne: Campaign?
        var campaignTwo: Campaign?

        runBlocking {
            val job = async { MockDataInDatabase(context).createAndInsertZoneOne() }
            zone = job.await()

            val job2 = async { MockDataInDatabase(context).createAndInsertCampaignTwo() }
            campaignTwo = job2.await()

            val job3 = async(ioDispatcher) { zoneRepository.getZoneByHash(zone!!.hash) }
            zoneWithCampaigns = job3.await()

            // The ID of the CampaignTwo is not available into the list of id's campaigns of ZoneOne
            liveEvents.add(GeofenceEvent(zoneWithCampaigns as Zone, MockLocation(context).buildLocation(), GeofenceType.ENTER))
            GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)
            Assert.assertTrue(zoneWithCampaigns!!.campaigns!!.size == 1)
            Assert.assertFalse(zoneWithCampaigns!!.campaigns!![0] == campaignTwo!!.id)
            Assert.assertFalse(notificationManager.notified)

            val job4 = async { MockDataInDatabase(context).createAndInsertCampaignOne() }
            campaignOne = job4.await()

            val job5 = async(ioDispatcher) { zoneRepository.getZoneByHash(zone!!.hash) }
            zoneWithCampaigns = job5.await()

            // The ID of the CampaignOne is available into the list of id's campaigns of ZoneOne
            // But we have no trigger registered so we should not be notified
            liveEvents.clear()
            liveEvents.add(GeofenceEvent(zoneWithCampaigns as Zone, MockLocation(context).buildLocation(), GeofenceType.ENTER))
            GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)

            Assert.assertTrue(zoneWithCampaigns!!.campaigns!!.size == 1)
            Assert.assertTrue(zoneWithCampaigns!!.campaigns!![0] == campaignOne!!.id)
            Assert.assertFalse(notificationManager.notified)

            val job7 = async(ioDispatcher) { zoneRepository.getZoneByHash(zone!!.hash) }
            zoneWithCampaigns = job7.await()

            // We had a Trigger with onExit at False for CampaignOne
            // ID of CampaignOne if detectable into listOfCampaigns from Zone
            liveEvents.clear()
            liveEvents.add(GeofenceEvent(zoneWithCampaigns as Zone, MockLocation(context).buildLocation(), GeofenceType.ENTER))
            GeofenceDispatcher.dispatchGeofenceEvent(liveEvents)

            Assert.assertTrue(zoneWithCampaigns!!.campaigns!!.size == 1)
            Assert.assertTrue(zoneWithCampaigns!!.campaigns!![0] == campaignOne!!.id)
            Assert.assertTrue(notificationManager.notified)

            // We update the Trigger to make it on Exit for CampaignOne
            // Event is also exiting
            liveEvents.clear()
            liveEvents.add(GeofenceEvent(zoneWithCampaigns as Zone, MockLocation(context).buildLocation(), GeofenceType.EXIT))

            Assert.assertTrue(zoneWithCampaigns!!.campaigns!!.size == 1)
            Assert.assertTrue(zoneWithCampaigns!!.campaigns!![0] == campaignOne!!.id)
            Assert.assertTrue(notificationManager.notified)
        }
    }

    @After
    fun cleanUp() {
        db.close()
    }
}

class NotificationManagerListener(var test: String = "hey") : GeofenceListener {
    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        test = "EventReceived"
    }
}