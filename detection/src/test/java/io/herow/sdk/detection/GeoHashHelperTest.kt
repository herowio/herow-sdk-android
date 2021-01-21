package io.herow.sdk.detection

import android.location.Location
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class GeoHashHelperTest {
    private val expectedEncodedGeoHash = "u4pruydqqvj8"
    private lateinit var locationToEncode: Location

    private val encodedGeoHashToDecode = "ezs42"
    private lateinit var locationToDecode: Location

    @Before
    fun setUp() {
        locationToEncode = Location("Jutland, Denmark")
        locationToEncode.latitude = 57.64911
        locationToEncode.longitude = 10.40744

        locationToDecode = Location("León, Spain")
        locationToDecode.latitude = 42.583
        locationToDecode.longitude = -5.625
    }

    @Test
    fun testEncode() {
        val geoHash = GeoHashHelper.encodeBase32(locationToEncode.latitude, locationToEncode.longitude)
        Assert.assertEquals(expectedEncodedGeoHash, geoHash)
    }

    @Test
    fun testDecode() {
        val location = GeoHashHelper.decodeBase32(encodedGeoHashToDecode)
        Assert.assertEquals(locationToDecode.latitude, location.latitude, 0.001)
        Assert.assertEquals(locationToDecode.longitude, location.longitude, 0.001)
    }
}