package io.herow.sdk.detection.analytics.model

import io.herow.sdk.detection.geofencing.GeofenceEvent

class HerowLogEnter(appState: String,
                    geofenceEvent: GeofenceEvent): HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
    }

    init {
        this[SUBTYPE] = LogSubtype.CONTEXT_REALTIME
        this[APP_STATE] = appState
        this[LOCATION] = geofenceEvent.location
    }
}