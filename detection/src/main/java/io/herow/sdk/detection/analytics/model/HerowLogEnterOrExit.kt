package io.herow.sdk.detection.analytics.model

import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.geofencing.model.LocationMediator

class HerowLogEnterOrExit(
    appState: String,
    geofenceEvent: GeofenceEvent
) : HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
        const val PLACE = "place"
    }

    init {
        if (geofenceEvent.type == GeofenceType.ENTER) {
            this[SUBTYPE] = LogSubtype.GEOFENCE_ENTER
        } else {
            this[SUBTYPE] = LogSubtype.GEOFENCE_EXIT
        }
        this[APP_STATE] = appState
        this[LOCATION] = LocationMediator(
            geofenceEvent.location.speed,
            geofenceEvent.location.accuracy,
            geofenceEvent.location.longitude,
            geofenceEvent.location.latitude,
            geofenceEvent.location.time
        )
        this[PLACE] = geofenceEvent.zone
    }
}