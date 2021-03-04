package io.herow.sdk.detection.analytics

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.analytics.model.HerowLogEnter
import io.herow.sdk.detection.analytics.model.HerowLogVisit
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.location.LocationListener

/**
 * Generate the followings logs (CONTEXT, GEOFENCE_ENTER/EXIT or VISIT) by listening to different
 * geofencing listeners.
 */
class LogGeneratorEvent(private val applicationData: ApplicationData,
                        private val sessionHolder: SessionHolder):
    AppStateListener, CacheListener, LocationListener, GeofenceListener {
    companion object {
        private const val DISTANCE_POI_MAX = 20_000
    }
    private var appState: String = "bg"
    private val cachePois = ArrayList<Poi>()
    private val listOfTemporaryLogsVisit = ArrayList<HerowLogVisit>()

    override fun onAppInForeground() {
        appState = "fg"
    }

    override fun onAppInBackground() {
        appState = "bg"
    }

    override fun onLocationUpdate(location: Location) {
        val herowLogContext = HerowLogContext(appState, location, computeNearbyPois(location))
        herowLogContext.enrich(applicationData, sessionHolder)
        val listOfLogs = listOf(Log(herowLogContext, TimeHelper.getCurrentTime()))
        LogsDispatcher.dispatchLogsResult(listOfLogs)
    }

    private fun computeNearbyPois(location: Location): List<Poi> {
        val closestPois: MutableList<Poi> = ArrayList()
        for (cachePoi in cachePois) {
            cachePoi.updateDistance(location)
            if (cachePoi.distance <= DISTANCE_POI_MAX) {
                closestPois.add(cachePoi)
            }
        }
        closestPois.sortBy {
            it.distance
        }
        return closestPois
    }

    override fun onCacheReception(cacheResult: CacheResult) {
        cachePois.clear()
        cachePois.addAll(cacheResult.pois)
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        val listOfLogsEnter = ArrayList<Log>()
        val listOfLogsVisit = ArrayList<Log>()

        for (geofenceEvent in geofenceEvents) {
            val nearbyPois = computeNearbyPois(geofenceEvent.location)
            val herowLogEnter = HerowLogEnter(appState, geofenceEvent, nearbyPois)
            herowLogEnter.enrich(applicationData, sessionHolder)
            listOfLogsEnter.add(Log(herowLogEnter, TimeHelper.getCurrentTime()))

            if (geofenceEvent.type == GeofenceType.ENTER) {
                val logVisit = HerowLogVisit(appState, geofenceEvent, nearbyPois)
                listOfTemporaryLogsVisit.add(logVisit)
            } else {
                for (logVisit in listOfTemporaryLogsVisit) {
                    if (geofenceEvent.zone.hash == logVisit[HerowLogVisit.PLACE_ID]) {
                        logVisit.updateDuration()
                        logVisit.enrich(applicationData, sessionHolder)
                        listOfLogsVisit.add(Log(logVisit, TimeHelper.getCurrentTime()))
                        listOfTemporaryLogsVisit.remove(logVisit)
                    }
                }
            }
        }

        LogsDispatcher.dispatchLogsResult(listOfLogsEnter)
        LogsDispatcher.dispatchLogsResult(listOfLogsVisit)
    }
}