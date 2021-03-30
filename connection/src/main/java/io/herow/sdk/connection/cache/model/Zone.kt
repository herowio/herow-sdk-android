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
    @Expose(serialize = false)
    @PrimaryKey(autoGenerate = true)
    val zoneID: Long = 0,

    @Expose
    @SerializedName(value = "place_id", alternate = ["hash"])
    var hash: String = "",

    @Expose
    var lat: Double? = 0.0,
    @Expose
    var lng: Double? = 0.0,
    @Expose
    var radius: Double? = 0.0,

    @Expose(serialize = false)
    var campaigns: List<String>? = listOf(),

    @Embedded
    @Expose(serialize = false)
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