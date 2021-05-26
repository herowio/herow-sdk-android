package io.herow.sdk.detection.analytics

import android.content.Context
import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.model.mapper.PoiMapper
import io.herow.sdk.connection.cache.model.mapper.ZoneMapper
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.analytics.model.HerowLogEnterOrExitorNotification
import io.herow.sdk.detection.analytics.model.HerowLogVisit
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.location.LocationListener
import io.herow.sdk.detection.notification.NotificationDispatcher
import io.herow.sdk.detection.notification.NotificationListener
import kotlinx.coroutines.*

/**
 * Generate the followings logs (CONTEXT, GEOFENCE_ENTER/EXIT, VISIT or GEOFENCE_ZONE_NOTIFICATION) by listening to different
 * geofencing listeners.
 */
class LogGeneratorEvent(
    private val applicationData: ApplicationData,
    private val sessionHolder: SessionHolder,
    val context: Context
) :
    AppStateListener, CacheListener, LocationListener, GeofenceListener, NotificationListener {

    companion object {
        private const val DISTANCE_POI_MAX = 20_000
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    init {
        NotificationDispatcher.addNotificationListener(this)
    }

    private val ioDispatcher = Dispatchers.IO
    private var appState: String = "bg"
    var cachePois = ArrayList<Poi>()
    var cacheZones = ArrayList<Zone>()
    private val db: HerowDatabase = HerowDatabase.getDatabase(context)

    private val listOfTemporaryLogsVisit = ArrayList<HerowLogVisit>()

    override fun onAppInForeground() {
        appState = "fg"
    }

    override fun onAppInBackground() {
        appState = "bg"
    }

    override fun onLocationUpdate(location: Location) {
        GlobalLogger.shared.info(context, "onLocationUpdate method is called")
        GlobalLogger.shared.info(context, "Location is: $location")

        var nearbyPois = computeNearbyPois(location)
        if (nearbyPois.size > 10) {
            nearbyPois = nearbyPois.subList(0, 10)
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
        GlobalLogger.shared.info(context, "List of logs are:  $listOfLogs")

        LogsDispatcher.dispatchLogsResult(listOfLogs)
    }

    private fun computeNearbyPois(location: Location): List<PoiMapper> {
        val closestPois: MutableList<PoiMapper> = ArrayList()
        for (cachePoi in cachePois) {
            cachePoi.updateDistance(location)
            if (cachePoi.distance <= DISTANCE_POI_MAX) {
                closestPois.add(
                    PoiMapper(
                        cachePoi.id,
                        cachePoi.distance,
                        cachePoi.tags
                    )
                )
            }
        }
        closestPois.sortBy {
            it.distance
        }
        GlobalLogger.shared.info(context, "Closests POIS are: $closestPois")

        return closestPois
    }

    private fun computeNearbyPlaces(location: Location): List<ZoneMapper> {
        val closestZones: MutableList<ZoneMapper> = ArrayList()

        for (cacheZone in cacheZones) {
            cacheZone.updateDistance(location)
            if (cacheZone.distance <= DISTANCE_POI_MAX) {
                closestZones.add(
                    ZoneMapper(
                        cacheZone.lng,
                        cacheZone.lat,
                        cacheZone.hash,
                        cacheZone.distance,
                        cacheZone.radius
                    )
                )
            }
        }

        closestZones.sortBy {
            it.distance
        }

        GlobalLogger.shared.info(context, "Closests zones are: $closestZones")
        return closestZones
    }

    override fun onCacheReception() {
        cachePois.clear()
        cacheZones.clear()
        GlobalLogger.shared.info(context, "CachePois before fetching are: $cachePois")

        runBlocking {
            val job = async(ioDispatcher) {
                retrievePois().let { cachePois.addAll(it) }
                retrieveZones().let { cacheZones.addAll(it) }
            }

            job.await()
        }

        GlobalLogger.shared.info(context, "CachePois after fetching are: $cachePois")
        GlobalLogger.shared.info(context, "CacheZones after fetching are: $cacheZones")
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        val listOfLogsEnterOrExit = ArrayList<Log>()
        val listOfLogsVisit = ArrayList<Log>()

        for (geofenceEvent in geofenceEvents) {
            val herowLogEnterOrExit = HerowLogEnterOrExitorNotification(appState, geofenceEvent)
            herowLogEnterOrExit.enrich(applicationData, sessionHolder)
            listOfLogsEnterOrExit.add(Log(herowLogEnterOrExit))

            if (geofenceEvent.type == GeofenceType.ENTER) {
                val logVisit = HerowLogVisit(appState, geofenceEvent)

                val iterator = listOfTemporaryLogsVisit.iterator()
                if (listOfTemporaryLogsVisit.size != 0) {
                    while (iterator.hasNext()) {
                        val item = iterator.next()
                        for (logTemporary in iterator) {
                            if (item[HerowLogVisit.PLACE_ID] != logTemporary[HerowLogVisit.PLACE_ID]) {
                                listOfTemporaryLogsVisit.add(logVisit)
                            }
                        }
                    }
                } else {
                    listOfTemporaryLogsVisit.add(logVisit)
                }
                GlobalLogger.shared.info(context, "LogVisit is $logVisit")
            } else {
                val logsToRemove = ArrayList<HerowLogVisit>()
                for (logVisit in listOfTemporaryLogsVisit) {
                    if (geofenceEvent.zone.hash == logVisit[HerowLogVisit.PLACE_ID]) {
                        logVisit.updateDuration()
                        logVisit.enrich(applicationData, sessionHolder)
                        listOfLogsVisit.add(Log(logVisit))
                        logsToRemove.add(logVisit)
                    }
                }
                listOfTemporaryLogsVisit.removeAll(logsToRemove)
            }
        }
        LogsDispatcher.dispatchLogsResult(listOfLogsEnterOrExit)
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

    override fun onNotificationSent(geofenceEvent: GeofenceEvent, campaign: Campaign) {
        GlobalLogger.shared.info(context, "Geofence of notification is: $geofenceEvent")

        val herowLogNotification = HerowLogEnterOrExitorNotification(appState, geofenceEvent, campaign)
        herowLogNotification.enrich(applicationData, sessionHolder)

        val logToSend = Log(herowLogNotification)

        GlobalLogger.shared.info(context, "Log notification is: $logToSend")
        LogsDispatcher.dispatchLogsResult(arrayListOf(logToSend))
    }
}