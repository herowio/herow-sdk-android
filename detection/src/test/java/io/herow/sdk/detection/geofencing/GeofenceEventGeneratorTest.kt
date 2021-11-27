package io.herow.sdk.detection.geofencing

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class GeofenceEventGeneratorTest: ICustomKoinTestComponent {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sessionHolder: SessionHolder by inject()

    private val herowGeofenceListener = HerowGeofenceListener()
    private lateinit var geofenceEventGenerator: GeofenceEventGenerator

    private val mockLocation = MockLocation()

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)

        sessionHolder.reset()

        GeofenceDispatcher.reset()
        geofenceEventGenerator = GeofenceEventGenerator()
        GeofenceDispatcher.addGeofenceListener(herowGeofenceListener)
    }

    @Test
    fun testDetectedZones() {
        val zones = ArrayList<Zone>()
        val firstZone = mockLocation.buildZone()
        val secondZone = mockLocation.buildZone()
        val thirdZone = mockLocation.buildZone()

        // No events at the beginning
        Assert.assertTrue(herowGeofenceListener.lastEvents.isEmpty())

        // Now, we have one zone, and we dispatch one event
        zones.add(firstZone)
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.ENTER, herowGeofenceListener.lastEvents[0].type)

        // We dispatch another event with another location but still detected in zone
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())
        Assert.assertTrue(herowGeofenceListener.lastEvents.size == 1)

        // Now, we exit the zone
        zones.clear()
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.EXIT, herowGeofenceListener.lastEvents[0].type)

        // We arrived in two zones
        zones.add(secondZone)
        zones.add(thirdZone)
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())
        Assert.assertEquals(2, herowGeofenceListener.lastEvents.size)

        // We exit the third zone, but still in the second zone
        zones.removeLast()
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())

        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.EXIT, herowGeofenceListener.lastEvents[0].type)

        // Still in second zone
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())
        Assert.assertTrue(herowGeofenceListener.lastEvents.size == 1)

        // Exit the second zone
        zones.removeLast()
        geofenceEventGenerator.detectedZones(zones, mockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.EXIT, herowGeofenceListener.lastEvents[0].type)
    }

    @After
    fun cleanUp() {
        GeofenceDispatcher.unregisterGeofenceListener(herowGeofenceListener)
    }
}

class HerowGeofenceListener : IGeofenceListener {
    var lastEvents: List<GeofenceEvent> = ArrayList()

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        lastEvents = geofenceEvents
    }
}