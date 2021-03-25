package io.herow.sdk.connection.cache.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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

    @Embedded
    val access: Access?
) {
    @Expose(deserialize = false)
    var distance: Double = 0.0

    fun toLocation(): Location {
        val location = Location(hash)
        location.latitude = lat!!
        location.longitude = lng!!
        return location
    }

    fun updateDistance(userLocation: Location) {
        val zoneLocation = toLocation()
        distance = zoneLocation.distanceTo(userLocation).toDouble()
    }
}