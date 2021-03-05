package io.herow.sdk.detection

import android.content.Context
import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.*

class MockLocation(private val context: Context) {
    private fun buildZone(
        lat: Double = RandomGenerator.lat(),
        lng: Double = RandomGenerator.lng(),
        zoneId: Long = RandomGenerator.id(),
        radius: Int = RandomGenerator.randomInt()
    ): Zone {
        return Zone(zoneId, "", lat, lng, radius, null, buildAccess())
    }

    private fun buildAccess(
        name: String = RandomGenerator.alphanumericalString(),
        address: String = RandomGenerator.alphanumericalString(45)
    ): Access {
        return Access(RandomGenerator.idString(), name, address)
    }

    fun buildLocation(
        lat: Double = RandomGenerator.lat(),
        lng: Double = RandomGenerator.lng()
    ): Location {
        val location = Location(RandomGenerator.idString())
        location.latitude = lat
        location.longitude = lng
        location.time = TimeHelper.getCurrentTime()
        return location
    }

    fun fetchZone(): Zone? {
        val db = HerowDatabase.getDatabase(context)
        val zoneRepository = ZoneRepository(db.zoneDAO())
        var zone: Zone? = buildZone()

        val job: Job = CoroutineScope(Dispatchers.IO).launch {
            zone = zoneRepository.getAllZones()!![0]
        }

        runBlocking {
            job.join()
        }

        return zone
    }
}