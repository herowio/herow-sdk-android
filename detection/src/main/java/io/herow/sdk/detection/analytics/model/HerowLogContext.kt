package io.herow.sdk.detection.analytics.model

import android.location.Location
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.detection.location.ClickAndCollectWorker

class HerowLogContext(appState: String,
                      location: Location,
                      nearbyPois: List<Poi> = ArrayList()): HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
        const val NEAR_BY_POIS = "nearbyPois"
    }
    init {
        if (ClickAndCollectWorker.isClickAndCollectInProgress) {
            this[SUBTYPE] = LogSubtype.CONTEXT_REALTIME
        } else {
            this[SUBTYPE] = LogSubtype.CONTEXT
        }
        this[APP_STATE] = appState
        this[LOCATION] = location
        this[NEAR_BY_POIS] = nearbyPois
    }
}