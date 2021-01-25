package io.herow.sdk.detection.analytics.model

import android.location.Location
import io.herow.sdk.detection.location.ClickAndCollectWorker

class HerowLogContext(appState: String,
                      location: Location): HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
    }
    init {
        if (ClickAndCollectWorker.isClickAndCollectInProgress) {
            this[SUBTYPE] = LogSubtype.CONTEXT_REALTIME
        } else {
            this[SUBTYPE] = LogSubtype.CONTEXT
        }
        this[APP_STATE] = appState
        this[LOCATION] = location
    }
}