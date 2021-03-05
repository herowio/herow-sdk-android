package io.herow.sdk.connection.cache.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Zone"
)
data class Zone(
    @PrimaryKey(autoGenerate = true)
    val zoneID: Long = 0,

    var hash: String? = "",
    var lat: Double? = 0.0,
    var lng: Double? = 0.0,
    var radius: Int? = 0,
    var campaigns: List<String>? = listOf(),

    @Embedded
    val access: Access?
) {
    fun toLocation(): Location {
        val location = Location(hash)
        location.latitude = lat!!
        location.longitude = lng!!
        return location
    }
}