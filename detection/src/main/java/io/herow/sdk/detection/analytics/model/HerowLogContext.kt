package io.herow.sdk.detection.analytics.model

import android.location.Location
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.mapper.PoiMapper
import io.herow.sdk.connection.cache.model.mapper.ZoneMapper
import io.herow.sdk.detection.geofencing.model.LocationMapper

class HerowLogContext(
    sessionHolder: SessionHolder,
    appState: String,
    location: Location,
    nearbyPois: List<PoiMapper> = ArrayList(),
    nearbyPlaces: List<ZoneMapper> = ArrayList()
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
        this[LOCATION] = LocationMapper(
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