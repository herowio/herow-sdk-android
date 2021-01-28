package io.herow.sdk.detection.analytics.model

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.Poi
import io.herow.sdk.detection.geofencing.GeofenceEvent

class HerowLogVisit(appState: String,
                    geofenceEvent: GeofenceEvent,
                    nearbyPois: List<Poi> = ArrayList()): HerowLogData() {
    private var duration = TimeHelper.getCurrentTime()

    companion object {
        const val LOCATION = "lastLocation"
        const val PLACE_ID = "place_id"
        const val NEAR_BY_POIS = "nearbyPois"
        const val DURATION = "duration"
    }

    init {
        this[SUBTYPE] = LogSubtype.VISIT
        this[APP_STATE] = appState
        this[LOCATION] = geofenceEvent.location
        this[NEAR_BY_POIS] = nearbyPois
        this[PLACE_ID] = geofenceEvent.zone.hash
        this[DURATION] = duration
    }

    fun updateDuration(timeBeforeSendingEvent: Long = TimeHelper.getCurrentTime()) {
        this[DURATION] = timeBeforeSendingEvent - duration
    }
}