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
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(index = true)
    var id: String = "",
    var lat: Double? = 0.0,
    var lng: Double? = 0.0,
    var tags: List<String>? = null
) {

    @Expose(deserialize = false)
    var distance: Float = 0f

    fun updateDistance(userLocation: Location): Float {
        val poiLocation = Location(id)
        poiLocation.latitude = lat!!
        poiLocation.longitude = lng!!
        return poiLocation.distanceTo(userLocation)
    }
}