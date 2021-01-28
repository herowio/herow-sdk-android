package io.herow.sdk.detection.analytics

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
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

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val applicationData = ApplicationData(context)
        val sessionHolder = SessionHolder(DataHolder(context))
        logGeneratorEvent = LogGeneratorEvent(applicationData, sessionHolder)
        LogsDispatcher.addLogListener(herowLogsListener)
    }

    @Test
    fun testGeofenceEvent(): Unit = runBlocking {
        val firstZone = MockLocation.buildZone()
        val secondZone = MockLocation.buildZone()
        val firstGeofenceEvent = GeofenceEvent(firstZone, MockLocation.buildLocation(), GeofenceType.ENTER)
        val secondGeofenceEvent = GeofenceEvent(secondZone, MockLocation.buildLocation(), GeofenceType.ENTER)
        logGeneratorEvent.onGeofenceEvent(listOf(firstGeofenceEvent, secondGeofenceEvent))
        Assert.assertEquals(0, herowLogsListener.herowLogsVisit.size)

        delay(500)

        val updatedFirstGeofenceEvent = GeofenceEvent(firstZone, MockLocation.buildLocation(), GeofenceType.EXIT)
        logGeneratorEvent.onGeofenceEvent(listOf(updatedFirstGeofenceEvent, secondGeofenceEvent))
        Assert.assertEquals(1, herowLogsListener.herowLogsVisit.size)
        val herowLogVisit = herowLogsListener.herowLogsVisit.first()
        Assert.assertEquals(herowLogVisit[HerowLogVisit.PLACE_ID], firstZone.hash)
        (herowLogVisit[HerowLogVisit.DURATION] as? Long)?.let { visitDuration: Long ->
            assertThat("Log visit duration in zone", visitDuration, greaterThan(500L))
        }

        delay(1_000)

        val updatedSecondGeofenceEvent = GeofenceEvent(secondZone, MockLocation.buildLocation(), GeofenceType.EXIT)
        logGeneratorEvent.onGeofenceEvent(listOf(updatedSecondGeofenceEvent))
        Assert.assertEquals(1, herowLogsListener.herowLogsVisit.size)
        val otherLogVisit = herowLogsListener.herowLogsVisit.first()
        Assert.assertEquals(otherLogVisit[HerowLogVisit.PLACE_ID], secondZone.hash)
        (otherLogVisit[HerowLogVisit.DURATION] as? Long)?.let { visitDuration: Long ->
            assertThat("Log visit duration in zone", visitDuration, greaterThan(500L))
        }
    }

    class HerowLogsListener: LogsListener {
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