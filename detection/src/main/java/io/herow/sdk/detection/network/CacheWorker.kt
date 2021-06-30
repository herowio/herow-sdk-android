package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.connection.database.HerowDatabaseHelper
import io.herow.sdk.detection.geofencing.model.LocationMapper
import io.herow.sdk.detection.geofencing.model.toLocation
import io.herow.sdk.detection.zones.ZoneManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Allow user to receive the zones to monitor and the pois to add in the HerowLogContext from the
 * Herow platform. You need to use a geohash to call the corresponding API.
 */
class CacheWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private var inputGeoHash = inputData.getString(KEY_GEOHASH) ?: ""

    private val zoneRepository = HerowDatabaseHelper.getZoneRepository(context)
    private val poiRepository = HerowDatabaseHelper.getPoiRepository(context)
    private val campaignRepository = HerowDatabaseHelper.getCampaignRepository(context)

    companion object {
        const val KEY_GEOHASH = "detection.geohash"
    }

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val authRequest = AuthRequests(sessionHolder, inputData)
        val database: HerowDatabase = HerowDatabase.getDatabase(context)

        GlobalLogger.shared.info(context, "Inside doWork() from CacheWorker")
        GlobalLogger.shared.info(context, "InputData $inputData")

        val locationMapper: LocationMapper = inputData.getString(Constants.LOCATION_DATA).let {
            GsonProvider.fromJson(it!!, LocationMapper::class.java)
        }

        GlobalLogger.shared.info(context, "Location from JSON is: $locationMapper")

        if (!sessionHolder.getOptinValue()) {
            GlobalLogger.shared.info(context, "Optin value is set to false")
            return Result.failure()
        }

        authRequest.execute {
            withContext(ioDispatcher) {
                GlobalLogger.shared.info(context, "Launching cacheRequest")
                launchCacheRequest(
                    sessionHolder,
                    authRequest.getHerowAPI(),
                    database,
                    locationMapper
                )
            }
        }

        return Result.success()
    }

    /**
     * If GeoHash is unknown or different from the saved one, cache is updated
     * If cache interval has been reached, cache is updated
     */
    private suspend fun launchCacheRequest(
        sessionHolder: SessionHolder,
        herowAPI: HerowAPI,
        database: HerowDatabase,
        locationMapper: LocationMapper
    ) {
        val extractedGeoHash = extractGeoHash(sessionHolder)
        if (extractedGeoHash.isNotEmpty()) {
            val cacheResponse = herowAPI.cache(extractedGeoHash.substring(0, 4))
            GlobalLogger.shared.info(context, "Cache response is $cacheResponse")

            if (cacheResponse.isSuccessful) {
                GlobalLogger.shared.info(context, "CacheResponse is successful")

                sessionHolder.saveLastLaunchCacheRequest(TimeHelper.getCurrentTime())

                cacheResponse.body()?.let { cacheResult: CacheResult? ->
                    GlobalLogger.shared.info(
                        context,
                        "Cache response body: ${cacheResponse.body()}"
                    )
                    GlobalLogger.shared.info(context, "CacheResult is $cacheResult")
                    withContext(ioDispatcher) {
                        println("Database is: $database")
                        database.clearAllTables()
                        GlobalLogger.shared.info(context, "Database has been cleared")

                        for (zone in cacheResult!!.zones) {
                            GlobalLogger.shared.info(context, "Zone is: $zone")
                        }

                        saveCacheDataInDB(cacheResult)
                        GlobalLogger.shared.info(context, "CacheResult has been saved in BDD")

                        CacheDispatcher.dispatch()
                        GlobalLogger.shared.info(context, "Dispatching zones")

                        GlobalLogger.shared.info(context, "Sending notification")
                        sendNotification(locationMapper)
                    }
                }
            }
        }
    }

    /**
     * Save geohash into session
     */
    private fun extractGeoHash(sessionHolder: SessionHolder): String {
        if (inputGeoHash.isNotEmpty()) {
            sessionHolder.saveGeohash(inputGeoHash)
            return inputGeoHash
        }

        return ""
    }

    private fun saveCacheDataInDB(cacheResult: CacheResult) {
        GlobalLogger.shared.info(context, "Cache result is: $cacheResult")
        if (!cacheResult.zones.isNullOrEmpty()) {
            for (zone in cacheResult.zones) {
                zoneRepository.insert(zone)
            }
            GlobalLogger.shared.info(context, "Zones have been saved in DB")
        }

        if (!cacheResult.pois.isNullOrEmpty()) {
            for (poi in cacheResult.pois) {
                poiRepository.insert(poi)
            }
            GlobalLogger.shared.info(context, "Pois have been saved in DB")
        }

        if (!cacheResult.campaigns.isNullOrEmpty()) {
            for (campaign in cacheResult.campaigns) {
                campaignRepository.insert(campaign)
            }
            GlobalLogger.shared.info(context, "Campaigns have been saved in DB")
        }
    }

    private fun sendNotification(locationMapper: LocationMapper) {
        val zoneRepository = HerowDatabaseHelper.getZoneRepository(context)
        val zonesResult = zoneRepository.getAllZones() as ArrayList<Zone>
        val location = LocationMapper().toLocation(locationMapper)

        GlobalLogger.shared.info(context, "ZonesResult is $zonesResult")
        GlobalLogger.shared.info(context, "Location is $location")

        ZoneManager(context, zonesResult).dispatchZonesAndNotification(location)
    }
}