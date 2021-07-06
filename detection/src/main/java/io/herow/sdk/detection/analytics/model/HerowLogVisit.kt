package io.herow.sdk.detection.analytics.model

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.model.LocationMapper

class HerowLogVisit(
    appState: String,
    geofenceEvent: GeofenceEvent
) : HerowLogData() {
    private var duration: Long = TimeHelper.getCurrentTime()

    companion object {
        const val LOCATION = "lastLocation"
        const val PLACE_ID = "place_id"
        const val DURATION = "duration"
    }

    init {
        this[SUBTYPE] = LogSubtype.GEOFENCE_VISIT
        this[APP_STATE] = appState
        this[LOCATION] = LocationMapper(
            geofenceEvent.location.speed,
            geofenceEvent.location.accuracy,
            geofenceEvent.location.longitude,
            geofenceEvent.location.latitude,
            geofenceEvent.location.time
        )
        this[PLACE_ID] = geofenceEvent.zone.hash
        this[DURATION] = duration
    }

    fun updateDuration(timeBeforeSendingEvent: Long = TimeHelper.getCurrentTime()) {
        this[DURATION] = timeBeforeSendingEvent - duration
    }
}