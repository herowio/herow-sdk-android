package io.herow.sdk.connection.cache.model

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(
    tableName = "POI"
)
data class Poi(
    @Expose
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(index = true)
    var id: String = "",
    @Expose(serialize = false)
    var lat: Double? = 0.0,
    @Expose(serialize = false)
    var lng: Double? = 0.0,
    @Expose
    var tags: List<String>? = null
) {

    @Expose(deserialize = false)
    var distance: Float = 0f

    fun updateDistance(userLocation: Location) {
        val poiLocation = Location(id)
        poiLocation.latitude = lat!!
        poiLocation.longitude = lng!!
        distance = poiLocation.distanceTo(userLocation)
    }
}