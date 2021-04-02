package io.herow.sdk.detection.analytics

import android.content.Context
import android.location.Location
import android.util.Log.i
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.analytics.model.HerowLogEnterOrExit
import io.herow.sdk.detection.analytics.model.HerowLogVisit
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.location.LocationListener
import kotlinx.coroutines.*

/**
 * Generate the followings logs (CONTEXT, GEOFENCE_ENTER/EXIT or VISIT) by listening to different
 * geofencing listeners.
 */
class LogGeneratorEvent(
    private val applicationData: ApplicationData,
    private val sessionHolder: SessionHolder,
    context: Context
) :
    AppStateListener, CacheListener, LocationListener, GeofenceListener {
    companion object {
        private const val DISTANCE_POI_MAX = 20_000
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private val ioDispatcher = Dispatchers.IO
    private var appState: String = "bg"
    var cachePois = ArrayList<Poi>()
    var cacheZones = ArrayList<Zone>()
    private val listOfTemporaryLogsVisit = ArrayList<HerowLogVisit>()
    private val db: HerowDatabase = HerowDatabase.getDatabase(context)

    override fun onAppInForeground() {
        appState = "fg"
    }

    override fun onAppInBackground() {
        appState = "bg"
    }

    override fun onLocationUpdate(location: Location) {
        i("XXX/EVENT", "LogGeneratorEvent - onLocationUpdate method called")
        i("XXX/EVENT", "LogGeneratorEvent - onLocationUpdate: Location is $location")

        var nearbyPois = computeNearbyPois(location)
        i("XXX/EVENT", "LogGeneratorEvent - Before sublist $nearbyPois")
        if (nearbyPois.size > 10) {
            nearbyPois = nearbyPois.subList(0, 10)
            i("XXX/EVENT", "LogGeneratorEvent - After sublist $nearbyPois")
        }

        val herowLogContext = HerowLogContext(
            sessionHolder,
            appState,
            location,
            nearbyPois,
            computeNearbyPlaces(location)
        )

        herowLogContext.enrich(applicationData, sessionHolder)
        val listOfLogs = listOf(Log(herowLogContext))
        i("XXX/EVENT", "LogGeneratorEvent - onLocationUpdate: ListofLogs are $listOfLogs")
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
        i("XXX/EVENT", "LogGeneratorEvent - Closests POIS are $closestPois")
        return closestPois
    }

    private fun computeNearbyPlaces(location: Location): List<Zone> {
        val closestZones: MutableList<Zone> = ArrayList()
        for (cacheZone in cacheZones) {
            cacheZone.updateDistance(location)
            if (cacheZone.distance <= DISTANCE_POI_MAX) {
                closestZones.add(cacheZone)
            }
        }

        closestZones.sortBy {
            it.distance
        }

        return closestZones
    }

    override fun onCacheReception() {
        cachePois.clear()
        cacheZones.clear()

        i("XXX/EVENT", "LogGeneratorEvent - cachePois before fetching are: $cachePois")

        runBlocking {
            val job = async(ioDispatcher) {
                retrievePois().let { cachePois.addAll(it) }
                retrieveZones().let { cacheZones.addAll(it) }
            }

            job.await()
        }

        i("XXX/EVENT", "LogGeneratorEvent - cachePois after fetching are: $cachePois")
        i("XXX/EVENT", "LogGeneratorEvent - cacheZones from BDD are: $cacheZones")
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        val listOfLogsEnter = ArrayList<Log>()
        val listOfLogsVisit = ArrayList<Log>()

        for (geofenceEvent in geofenceEvents) {
            val herowLogEnter = HerowLogEnterOrExit(appState, geofenceEvent)
            herowLogEnter.enrich(applicationData, sessionHolder)
            listOfLogsEnter.add(Log(herowLogEnter))

            i("", "")

            if (geofenceEvent.type == GeofenceType.ENTER) {
                val logVisit = HerowLogVisit(appState, geofenceEvent)
                listOfTemporaryLogsVisit.add(logVisit)
                i("XXX/EVENT", "LogGeneratorEvent - LogVisit is $logVisit")
            } else {
                for (logVisit in listOfTemporaryLogsVisit) {
                    if (geofenceEvent.zone.hash == logVisit[HerowLogVisit.PLACE_ID]) {
                        logVisit.updateDuration()
                        logVisit.enrich(applicationData, sessionHolder)
                        listOfLogsVisit.add(Log(logVisit))
                        listOfTemporaryLogsVisit.remove(logVisit)
                    }
                }
            }
        }
        LogsDispatcher.dispatchLogsResult(listOfLogsEnter)
        LogsDispatcher.dispatchLogsResult(listOfLogsVisit)
    }

    private suspend fun retrievePois(): ArrayList<Poi> {
        val poiRepository = PoiRepository(db.poiDAO())

        val poisInDB = scope.async(ioDispatcher) {
            poiRepository.getAllPois() as ArrayList<Poi>
        }

        return poisInDB.await()
    }

    private suspend fun retrieveZones(): ArrayList<Zone> {
        val zoneRepository = ZoneRepository(db.zoneDAO())

        val zonesInDb = scope.async(ioDispatcher) {
            zoneRepository.getAllZones() as ArrayList<Zone>
        }

        return zonesInDb.await()
    }
}