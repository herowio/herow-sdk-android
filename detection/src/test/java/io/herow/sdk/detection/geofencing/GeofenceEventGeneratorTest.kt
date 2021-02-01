package io.herow.sdk.detection.geofencing

import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.MockLocation
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.collections.ArrayList

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class GeofenceEventGeneratorTest {
    private val herowGeofenceListener = HerowGeofenceListener()
    private lateinit var geofenceEventGenerator: GeofenceEventGenerator

    @Before
    fun setUp() {
        geofenceEventGenerator = GeofenceEventGenerator()
        GeofenceDispatcher.addGeofenceListener(herowGeofenceListener)
    }

    @Test
    fun testDetectedZones() {
        val zones = ArrayList<Zone>()
        val firstZone = MockLocation.buildZone()
        val secondZone = MockLocation.buildZone()
        val thirdZone = MockLocation.buildZone()

        // No events at the beginning
        Assert.assertTrue(herowGeofenceListener.lastEvents.isEmpty())

        // Now, we have one zone, and we dispatch one event
        zones.add(firstZone)
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.ENTER, herowGeofenceListener.lastEvents[0].type)

        // We dispatch another event with another location but still detected in zone
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertTrue(herowGeofenceListener.lastEvents.isEmpty())

        // Now, we exit the zone
        zones.clear()
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.EXIT, herowGeofenceListener.lastEvents[0].type)

        // We arrived in two zones
        zones.add(secondZone)
        zones.add(thirdZone)
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertEquals(2, herowGeofenceListener.lastEvents.size)

        // We exit the third zone, but still in the second zone
        zones.removeLast()
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.EXIT, herowGeofenceListener.lastEvents[0].type)

        // Still in second zone
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertTrue(herowGeofenceListener.lastEvents.isEmpty())

        // Exit the second zone
        zones.removeLast()
        geofenceEventGenerator.detectedZones(zones, MockLocation.buildLocation())
        Assert.assertEquals(1, herowGeofenceListener.lastEvents.size)
        Assert.assertEquals(GeofenceType.EXIT, herowGeofenceListener.lastEvents[0].type)
    }

    class HerowGeofenceListener: GeofenceListener {
        var lastEvents: List<GeofenceEvent> = ArrayList()

        override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
            lastEvents = geofenceEvents
        }
    }
}