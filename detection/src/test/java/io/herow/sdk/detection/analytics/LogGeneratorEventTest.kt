package io.herow.sdk.detection.analytics

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.analytics.model.HerowLogVisit
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class LogGeneratorEventTest {
    private val herowLogsListener = HerowLogsListener()
    private lateinit var logGeneratorEvent: LogGeneratorEvent

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val applicationData = ApplicationData(context)
        val sessionHolder = SessionHolder(DataHolder(context))
        logGeneratorEvent = LogGeneratorEvent(applicationData, sessionHolder, context)

        //CreateZone
        val fakeZone = Zone(
            hash = "ivbxbhxm8rnk",
            lat = 48.875741,
            lng = 2.349255,
            radius = 300.0,
            campaigns = null,
            access = Access(
                address = "54 Rue de Paradis, 75010 Paris, France",
                id = "6004957256eb6779115b6d8a",
                name = "HEROW"
            )
        )

        //CreatePOI
        val fakePOI = Poi(
            id = "7515771363",
            lat = 48.84748,
            lng = 2.35231,
            tags = arrayListOf("Tag1", "Tag2")
        )

        logGeneratorEvent.cacheZones = arrayListOf(fakeZone)
        logGeneratorEvent.cachePois = arrayListOf(fakePOI)

        LogsDispatcher.addLogListener(herowLogsListener)
    }

    @Test
    fun testGeofenceEvent(): Unit = runBlocking {
        val firstZone = Zone(
            hash = "ivbxbhxm8rnk",
            lat = 48.875741,
            lng = 2.349255,
            radius = 300.0,
            campaigns = null,
            access = Access(
                address = "54 Rue de Paradis, 75010 Paris, France",
                id = "6004957256eb6779115b6d8a",
                name = "HEROW"
            )
        )

        val secondZone = Zone(
            hash = "knr8mxhbxbvi",
            lat = 48.128177642822266,
            lng = -1.6853195428848267,
            radius = 300.0,
            campaigns = null,
            access = Access(
                address = "1 Rue Olivier de Serres, 35000 Rennes, France",
                id = "6004957256eb6779115b6123",
                name = "HOME MORGANE"
            )
        )

        val firstGeofenceEvent = GeofenceEvent(firstZone, MockLocation(context).buildLocation(), GeofenceType.ENTER)
        val secondGeofenceEvent = GeofenceEvent(secondZone, MockLocation(context).buildLocation(), GeofenceType.ENTER)
        logGeneratorEvent.onGeofenceEvent(listOf(firstGeofenceEvent, secondGeofenceEvent))
        Assert.assertEquals(0, herowLogsListener.herowLogsVisit.size)

        delay(500)

        val updatedFirstGeofenceEvent = GeofenceEvent(firstZone, MockLocation(context).buildLocation(), GeofenceType.EXIT)
        logGeneratorEvent.onGeofenceEvent(listOf(updatedFirstGeofenceEvent, secondGeofenceEvent))
        Assert.assertEquals(1, herowLogsListener.herowLogsVisit.size)

        val herowLogVisit = herowLogsListener.herowLogsVisit.first()
        Assert.assertEquals(herowLogVisit[HerowLogVisit.PLACE_ID], firstZone.hash)

        (herowLogVisit[HerowLogVisit.DURATION] as? Long)?.let { visitDuration: Long ->
            assertThat("Log visit duration in zone", visitDuration, greaterThan(500L))
        }

        delay(1_000)

        val updatedSecondGeofenceEvent = GeofenceEvent(secondZone, MockLocation(context).buildLocation(), GeofenceType.EXIT)
        logGeneratorEvent.onGeofenceEvent(listOf(updatedSecondGeofenceEvent))
        Assert.assertEquals(1, herowLogsListener.herowLogsVisit.size)

        val otherLogVisit = herowLogsListener.herowLogsVisit.first()
        Assert.assertEquals(otherLogVisit[HerowLogVisit.PLACE_ID], secondZone.hash)
        (otherLogVisit[HerowLogVisit.DURATION] as? Long)?.let { visitDuration: Long ->
            assertThat("Log visit duration in zone", visitDuration, greaterThan(500L))
        }
    }


    class HerowLogsListener : LogsListener {
        val herowLogsVisit = ArrayList<HerowLogVisit>()

        override fun onLogsToSend(listOfLogs: List<Log>) {
            for (log in listOfLogs) {
                (log.data as? HerowLogVisit)?.let {
                    herowLogsVisit.clear()
                }
            }
            for (log in listOfLogs) {
                (log.data as? HerowLogVisit)?.let {
                    herowLogsVisit.add(it)
                }
            }
        }
    }
}