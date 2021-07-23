package io.herow.sdk.connection.cache.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.herow.sdk.common.logger.GlobalLogger

@Entity(
    tableName = "Zone"
)
data class Zone(
    @PrimaryKey(autoGenerate = true)
    val zoneID: Long = 0,

    @SerializedName(value = "place_id", alternate = ["hash"])
    var hash: String = "",

    var lat: Double? = 0.0,
    var lng: Double? = 0.0,
    var radius: Double? = 0.0,
    var campaigns: List<String>? = listOf(),
    var confidence: Double? = null,
    var centerLocation: Location? = null,

    @Embedded
    val access: Access?
) {
    @Expose(deserialize = false)
    var distance: Double = 0.0

    fun updateDistance(userLocation: Location): Double {
        val zoneLocation = toLocation()
        GlobalLogger.shared.info(null, "ZoneLocation is: $zoneLocation")
        GlobalLogger.shared.info(null, "UserLocation is: $userLocation")

        return zoneLocation.distanceTo(userLocation).toDouble()
    }

    fun toLocation(): Location {
        return Location(hash).apply {
            latitude = lat!!
            longitude = lng!!
        }
    }

    fun isIn(location: Location, scale: Double = 1.0): Boolean {
        val zoneLocation = toLocation()
        val distanceToCenterOfZone = location.distanceTo(zoneLocation)

        val limit = radius!! * scale
        return distanceToCenterOfZone <= limit
    }
}