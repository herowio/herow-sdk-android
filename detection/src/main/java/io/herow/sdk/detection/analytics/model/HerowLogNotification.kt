package io.herow.sdk.detection.analytics.model

import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.mapper.ZoneMapper
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.model.LocationMapper

class HerowLogNotification (
    appState: String,
    geofenceEvent: GeofenceEvent,
    campaign: Campaign? = null
) : HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
        const val PLACE = "place"

    }

    init {
        this[HerowLogData.SUBTYPE] = LogSubtype.GEOFENCE_ZONE_NOTIFICATION
        this[HerowLogData.APP_STATE] = appState
        this[LOCATION] = LocationMapper(
            geofenceEvent.location.speed,
            geofenceEvent.location.accuracy,
            geofenceEvent.location.longitude,
            geofenceEvent.location.latitude,
            geofenceEvent.location.time
        )
        this[PLACE] = ZoneMapper(
            lng = geofenceEvent.zone.lng,
            lat = geofenceEvent.zone.lat,
            place_id = geofenceEvent.zone.hash,
            distance = geofenceEvent.zone.distance,
            radius = geofenceEvent.zone.radius
        )

        if (this[HerowLogData.SUBTYPE] == LogSubtype.GEOFENCE_ZONE_NOTIFICATION) {
            val CAMPAIGN_ID = "campaign_id"
            this[CAMPAIGN_ID] = campaign.let { campaign?.id } ?: ""
        }

    }
}