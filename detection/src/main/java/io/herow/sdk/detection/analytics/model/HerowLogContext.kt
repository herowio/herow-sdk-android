package io.herow.sdk.detection.analytics.model

import android.location.Location

class HerowLogContext(appState: String,
                      location: Location): HerowLogData() {
    companion object {
        const val LOCATION = "lastLocation"
    }
    init {
        this[SUBTYPE] = LogSubtype.CONTEXT
        this[APP_STATE] = appState
        this[LOCATION] = location
    }
}