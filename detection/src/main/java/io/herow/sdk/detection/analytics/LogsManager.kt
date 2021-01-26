package io.herow.sdk.detection.analytics

import android.content.Context
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.cache.CacheDispatcher
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.location.LocationDispatcher

class LogsManager(context: Context) {
    private val applicationData = ApplicationData(context)
    private val sessionHolder = SessionHolder(DataHolder(context))
    private val logGeneratorEvent = LogGeneratorEvent(applicationData, sessionHolder)

    init {
        AppStateDetector.addAppStateListener(logGeneratorEvent)
        LocationDispatcher.addLocationListener(logGeneratorEvent)
        CacheDispatcher.addCacheListener(logGeneratorEvent)
        GeofenceDispatcher.addGeofenceListener(logGeneratorEvent)
    }
}