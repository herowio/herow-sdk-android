package io.herow.sdk.detection.analytics

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheResult
import io.herow.sdk.connection.cache.Poi
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.analytics.model.HerowLogEnter
import io.herow.sdk.detection.cache.CacheListener
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.location.LocationListener
import kotlin.collections.ArrayList

class LogGeneratorEvent(private val applicationData: ApplicationData,
                        private val sessionHolder: SessionHolder):
    AppStateListener, CacheListener, LocationListener, GeofenceListener {
    companion object {
        private const val DISTANCE_POI_MAX = 20_000
    }
    private var appState: String = "bg"
    private val cachePois = ArrayList<Poi>()

    override fun onAppInForeground() {
        appState = "fg"
    }

    override fun onAppInBackground() {
        appState = "bg"
    }

    override fun onLocationUpdate(location: Location) {
        sendLogContext(location, computeNearbyPois(location))
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

    private fun sendLogContext(location: Location, nearbyPois: List<Poi>) {
        val herowLogContext = HerowLogContext(appState, location, nearbyPois)
        herowLogContext.enrich(applicationData, sessionHolder)

        val listOfLogs = listOf(Log(herowLogContext, TimeHelper.getCurrentTime()))
        val logs = Logs(listOfLogs)
        val logJsonString: String = GsonProvider.toJson(logs, Logs::class.java)

        HerowInitializer.launchLogsRequest(logJsonString)
    }

    override fun onCacheReception(cacheResult: CacheResult) {
        cachePois.clear()
        cachePois.addAll(cacheResult.pois)
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        sendGeofenceLog(geofenceEvents)
    }

    private fun sendGeofenceLog(geofenceEvents: List<GeofenceEvent>) {
        val listOfLogs = ArrayList<Log>()

        for (geofenceEvent in geofenceEvents) {
            val nearbyPois = computeNearbyPois(geofenceEvent.location)
            val herowLogEnter = HerowLogEnter(appState, geofenceEvent, nearbyPois)
            herowLogEnter.enrich(applicationData, sessionHolder)
            listOfLogs.add(Log(herowLogEnter, TimeHelper.getCurrentTime()))
        }

        val logs = Logs(listOfLogs)
        val logJsonString: String = GsonProvider.toJson(logs, Logs::class.java)

        HerowInitializer.launchLogsRequest(logJsonString)
    }
}