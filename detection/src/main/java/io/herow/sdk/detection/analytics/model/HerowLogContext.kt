package io.herow.sdk.detection.analytics.model

import android.location.Location
import com.google.gson.annotations.Expose
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.geofencing.model.LocationMediator

class HerowLogContext(
    sessionHolder: SessionHolder,
    appState: String,
    location: Location,
    nearbyPois: List<Poi> = ArrayList(),
    nearbyPlaces: List<Zone> = ArrayList()
) : HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
        const val NEAR_BY_POIS = "nearbyPois"
        const val NEAR_BY_PLACES = "nearby_places"
    }

    init {
        if (sessionHolder.getClickAndCollectProgress()) {
            this[SUBTYPE] = LogSubtype.CONTEXT_REALTIME
        } else {
            this[SUBTYPE] = LogSubtype.CONTEXT
        }
        this[APP_STATE] = appState
        this[LOCATION] = LocationMediator(
            location.speed,
            location.accuracy,
            location.longitude,
            location.latitude,
            location.time
        )
        this[NEAR_BY_POIS] = nearbyPois
        this[NEAR_BY_PLACES] = nearbyPlaces
    }
}