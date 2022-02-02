package io.herow.sdk.detection.analytics.model

import android.location.Location
import io.herow.sdk.common.helpers.LocationMapper
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.mapper.PoiMapper
import io.herow.sdk.connection.cache.model.mapper.ZoneMapper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import org.koin.core.component.inject

class HerowLogContext(
    appState: String,
    location: Location,
    nearbyPois: List<PoiMapper> = ArrayList(),
    nearbyPlaces: List<ZoneMapper> = ArrayList(),
    moments: Moments?
) : HerowLogData(), ICustomKoinComponent {

    private val sessionHolder: SessionHolder by inject()

    companion object {
        const val LOCATION = "lastLocation"
        const val NEAR_BY_POIS = "nearbyPois"
        const val NEAR_BY_PLACES = "nearby_places"
        const val MOMENTS = "moments"
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
        moments?.let { this[MOMENTS] = it }
    }
}