package io.herow.sdk.detection.analytics.model

import io.herow.sdk.connection.cache.Poi
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType

class HerowLogEnter(appState: String,
                    geofenceEvent: GeofenceEvent,
                    nearbyPois: List<Poi> = ArrayList()): HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
        const val PLACE_ID = "place_id"
        const val NEAR_BY_POIS = "nearbyPois"
    }

    init {
        if (geofenceEvent.type == GeofenceType.ENTER) {
            this[SUBTYPE] = LogSubtype.GEOFENCE_ENTER
        } else {
            this[SUBTYPE] = LogSubtype.GEOFENCE_EXIT
        }
        this[APP_STATE] = appState
        this[LOCATION] = geofenceEvent.location
        this[NEAR_BY_POIS] = nearbyPois
        this[PLACE_ID] = geofenceEvent.zone.hash
    }
}