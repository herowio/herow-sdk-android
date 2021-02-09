package io.herow.sdk.connection.cache.model

import android.location.Location
import com.google.gson.annotations.Expose

data class Poi(val id: String,
               @Expose(serialize = false)
               val lat: Double,
               @Expose(serialize = false)
               val lng: Double,
               val tags: List<String>) {

    @Expose(deserialize = false)
    var distance: Float = 0f

    fun updateDistance(userLocation: Location) {
        val poiLocation = Location(id)
        poiLocation.latitude = lat
        poiLocation.longitude = lng
        distance = poiLocation.distanceTo(userLocation)
    }
}