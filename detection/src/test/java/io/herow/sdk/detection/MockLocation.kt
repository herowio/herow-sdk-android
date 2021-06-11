package io.herow.sdk.detection

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Zone

class MockLocation {

     fun buildZone(
        lat: Double = RandomGenerator.lat(),
        lng: Double = RandomGenerator.lng(),
        zoneId: Long = RandomGenerator.id(),
        radius: Double = RandomGenerator.randomInt().toDouble(),
        hash: String = RandomGenerator.alphanumericalString()
    ): Zone {
        return Zone(zoneId, hash, lat, lng, radius, null, access = buildAccess())
    }

    private fun buildAccess(
        name: String = RandomGenerator.alphanumericalString(),
        address: String = RandomGenerator.alphanumericalString(45)
    ): Access {
        return Access(RandomGenerator.idString(), name, address)
    }

    fun buildLocation(
        lat: Double = 48.875516,
        lng: Double = 2.349096
    ): Location {
        val location = Location(RandomGenerator.idString())
        location.latitude = lat
        location.longitude = lng
        location.time = TimeHelper.getCurrentTime()
        return location
    }
}