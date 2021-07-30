package io.herow.sdk.detection.analytics

import android.content.Context
import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.IAppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.ICacheListener
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.model.mapper.PoiMapper
import io.herow.sdk.connection.cache.model.mapper.ZoneMapper
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.analytics.model.HerowLogEnterOrExit
import io.herow.sdk.detection.analytics.model.HerowLogNotification
import io.herow.sdk.detection.analytics.model.HerowLogVisit
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceType
import io.herow.sdk.detection.geofencing.IGeofenceListener
import io.herow.sdk.detection.location.ILocationListener
import io.herow.sdk.detection.notification.INotificationListener
import io.herow.sdk.detection.notification.NotificationDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Generate the followings logs (CONTEXT, GEOFENCE_ENTER/EXIT, VISIT or GEOFENCE_ZONE_NOTIFICATION) by listening to different
 * geofencing listeners.
 */
class LogGeneratorEvent(
    private val applicationData: ApplicationData,
    private val sessionHolder: SessionHolder,
    val context: Context
) :
    KoinComponent, IAppStateListener, ICacheListener, ILocationListener, IGeofenceListener,
    INotificationListener {

    companion object {
        private const val DISTANCE_MAX = 20_000
    }

    init {
        NotificationDispatcher.addNotificationListener(this)
    }

    private val dispatcher: CoroutineDispatcher by inject()
    private var appState: String = "bg"
    private val listOfTemporaryLogsVisit = ArrayList<HerowLogVisit>()
    private val zoneRepository: ZoneRepository by inject()
    private val poiRepository: PoiRepository by inject()
    var cachePois = ArrayList<Poi>()
    var cacheZones = ArrayList<Zone>()

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
        GlobalLogger.shared.info(context, "CachePois are: $cachePois")

        synchronized(cachePois) {
            for (cachePoi in cachePois) {
                cachePoi.distance = cachePoi.updateDistance(location)

                GlobalLogger.shared.info(context, "Distance POI is: $DISTANCE_MAX")
                GlobalLogger.shared.info(context, "CachePoi distance is: ${cachePoi.distance}")

                if (cachePoi.distance <= DISTANCE_MAX) {
                    closestPois.add(
                        PoiMapper(
                            cachePoi.id,
                            cachePoi.distance,
                            cachePoi.tags
                        )
                    )
                }
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

        GlobalLogger.shared.info(context, "CacheZones are: $cacheZones")

        synchronized(cacheZones) {
            for (cacheZone in cacheZones) {
                cacheZone.distance = cacheZone.updateDistance(location)
                GlobalLogger.shared.info(context, "CacheZone distance is: ${cacheZone.distance}")

                if (cacheZone.distance <= DISTANCE_MAX) {
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
            withContext(dispatcher) {
                retrievePois().let { cachePois.addAll(it) }
                retrieveZones().let { cacheZones.addAll(it) }
            }
        }

        GlobalLogger.shared.info(context, "CachePois after fetching are: $cachePois")
        GlobalLogger.shared.info(context, "CacheZones after fetching are: $cacheZones")
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        val listOfLogsEnterOrExit = ArrayList<Log>()
        val listOfLogsVisit = ArrayList<Log>()

        for (geofenceEvent in geofenceEvents) {
            geofenceEvent.zone.distance = geofenceEvent.zone.updateDistance(geofenceEvent.location)

            if (geofenceEvent.type == GeofenceType.GEOFENCE_NOTIFICATION_ENTER) {
                return
            }

            val herowLogEnterOrExit = HerowLogEnterOrExit(appState, geofenceEvent)
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

        val logs = ArrayList<Log>()
        if (listOfLogsEnterOrExit.isNotEmpty()) {
            GlobalLogger.shared.debug(context, "list of logEvents is: $listOfLogsEnterOrExit")
            logs.addAll(listOfLogsEnterOrExit)
        }
        if (listOfLogsVisit.isNotEmpty()) {
            logs.addAll(listOfLogsVisit)
            GlobalLogger.shared.debug(context, "list of logVisits is: $listOfLogsVisit")
        }

        LogsDispatcher.dispatchLogsResult(logs)
    }

    private suspend fun retrievePois(): ArrayList<Poi> = withContext(dispatcher) {
        poiRepository.getAllPois() as ArrayList<Poi>
    }

    private suspend fun retrieveZones(): ArrayList<Zone> = withContext(dispatcher) {
        zoneRepository.getAllZones() as ArrayList<Zone>
    }

    override fun onNotificationSent(geofenceEvent: GeofenceEvent, campaign: Campaign) {
        GlobalLogger.shared.info(context, "Geofence of notification is: $geofenceEvent")
        val herowLogNotification = HerowLogNotification(appState, geofenceEvent, campaign)
        herowLogNotification.enrich(applicationData, sessionHolder)
        val logToSend = Log(herowLogNotification)
        GlobalLogger.shared.info(context, "Log notification is: $logToSend")
        LogsDispatcher.dispatchLogsResult(arrayListOf(logToSend))
    }
}