package io.herow.sdk.detection.analytics.model

import android.location.Location
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone

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
        this[LOCATION] = location
        this[NEAR_BY_POIS] = nearbyPois
        this[NEAR_BY_PLACES] = nearbyPlaces
    }
}