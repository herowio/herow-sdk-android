package io.herow.sdk.connection.cache.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity

@Entity(
    tableName = "Zone"
)
data class Zone(
    val hash: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val listOfCampaigns: List<Campaign>?,

    @Embedded
    val access: Access
) {
    fun toLocation(): Location {
        val location = Location(hash)
        location.latitude = lat
        location.longitude = lng
        return location
    }
}