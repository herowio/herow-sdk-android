package io.herow.sdk.detection

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Zone

object MockLocation {
    fun buildZone(lat: Double = RandomGenerator.lat(),
                  lng: Double = RandomGenerator.lng(),
                  zoneId: String = RandomGenerator.id(),
                  radius: Int = RandomGenerator.randomInt()): Zone {
        return Zone(zoneId, lat, lng, radius, null, buildAccess())
    }

    fun buildAccess(name: String = RandomGenerator.alphanumericalString(),
                    address: String = RandomGenerator.alphanumericalString(45)): Access {
        return Access(RandomGenerator.id(), name, address)
    }

    fun buildLocation(lat: Double = RandomGenerator.lat(),
                      lng: Double = RandomGenerator.lng()): Location {
        val location = Location(RandomGenerator.id())
        location.latitude = lat
        location.longitude = lng
        location.time = TimeHelper.getCurrentTime()
        return location
    }
}