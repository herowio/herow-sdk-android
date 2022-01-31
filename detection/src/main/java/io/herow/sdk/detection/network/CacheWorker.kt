package io.herow.sdk.detection.network

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.geofencing.model.LocationMapper
import io.herow.sdk.detection.geofencing.model.toLocation
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.zones.ZoneManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Allow user to receive the zones to monitor and the pois to add in the HerowLogContext from the
 * Herow platform. You need to use a geohash to call the corresponding API.
 */
@Keep
class CacheWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), ICustomKoinComponent {

    var testing = false
    private var inputGeoHash = inputData.getString(KEY_GEOHASH) ?: ""

    private val db: HerowDatabase = Room.databaseBuilder(context, HerowDatabase::class.java, "herow_test_BDD").build()
    private val database: HerowDatabase by inject()
    private val zoneRepository: ZoneRepository by inject()
    private val poiRepository: PoiRepository by inject()
    private val campaignRepository: CampaignRepository by inject()

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val sessionHolder: SessionHolder by inject()
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    companion object {
        const val KEY_GEOHASH = "detection.geohash"
    }

    override suspend fun doWork(): Result {
        if (HerowInitializer.isTesting()) {
            HerowKoinTestContext.init(context)
        }

        val authRequest = AuthRequests(inputData)
        val locationMapper: LocationMapper = inputData.getString(Constants.LOCATION_DATA).let {
            GsonProvider.fromJson(it!!, LocationMapper::class.java)
        }

        GlobalLogger.shared.info(context, "Location from JSON is: $locationMapper")

        if (!sessionHolder.getOptinValue()) {
            GlobalLogger.shared.info(context, "Optin value is set to false")
            return Result.failure()
        }

        applicationScope.launch {
            authRequest.execute {
                try {
                    launchCacheRequest(authRequest.getHerowAPI(), locationMapper)
                } catch (exception: Throwable) {
                    println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
                }
            }
        }

        return Result.success()
    }

    /**
     * If GeoHash is unknown or different from the saved one, cache is updated
     * If cache interval has been reached, cache is updated
     */
    private suspend fun launchCacheRequest(
        herowAPI: IHerowAPI,
        locationMapper: LocationMapper
    ) {
        val extractedGeoHash = extractGeoHash()

        if (extractedGeoHash.isNotEmpty()) {
            val cacheResponse = herowAPI.cache(extractedGeoHash.substring(0, 4))

            GlobalLogger.shared.info(context, "Cache response is $cacheResponse")
            GlobalLogger.shared.info(context, "Cache headers are ${cacheResponse.headers()}")

            if (cacheResponse.isSuccessful) {
                sessionHolder.saveLastLaunchCacheRequest(TimeHelper.getCurrentTime())

                cacheResponse.body()?.let { cacheResult: CacheResult? ->
                    GlobalLogger.shared.info(context, "Cache response body: ${cacheResponse.body()}")
                    GlobalLogger.shared.info(context, "CacheResult is $cacheResult")

                    if (!HerowInitializer.isTesting()) {
                        database.clearAllTables()
                        GlobalLogger.shared.info(context, "Database has been cleared")
                    }

                    saveCacheDataInDB(cacheResult!!)

                    CacheDispatcher.dispatch()
                    sendNotification(locationMapper)
                }
            }
        }
    }

    /**
     * Save geohash into session
     */
    private fun extractGeoHash(): String {
        if (inputGeoHash.isNotEmpty()) {
            sessionHolder.saveGeohash(inputGeoHash)
            return inputGeoHash
        }

        return ""
    }

    private fun saveCacheDataInDB(cacheResult: CacheResult) {
        GlobalLogger.shared.info(context, "Cache result is: $cacheResult")

        if (HerowInitializer.isTesting()) {
            db.clearAllTables()
        }

        if (cacheResult.zones.isNotEmpty()) {
            for (zone in cacheResult.zones) {
                if (HerowInitializer.isTesting()) {
                    ZoneRepository(db.zoneDAO()).insert(zone)
                } else {
                    zoneRepository.insert(zone)
                }
            }
            GlobalLogger.shared.info(context, "Zones have been saved in DB")
        }

        if (cacheResult.pois.isNotEmpty()) {
            for (poi in cacheResult.pois) {
                if (HerowInitializer.isTesting()) {
                    PoiRepository(db.poiDAO()).insert(poi)
                } else {
                    poiRepository.insert(poi)
                }
            }
            GlobalLogger.shared.info(context, "Pois have been saved in DB")
        }

        if (cacheResult.campaigns.isNotEmpty()) {
            for (campaign in cacheResult.campaigns) {
                if (HerowInitializer.isTesting()) {
                    CampaignRepository(db.campaignDAO()).insert(campaign)
                } else {
                    campaignRepository.insert(campaign)
                }
            }
            GlobalLogger.shared.info(context, "Campaigns have been saved in DB")
        }
    }

    private fun sendNotification(locationMapper: LocationMapper) {
        val zonesResult = if (HerowInitializer.isTesting()) {
            ZoneRepository(db.zoneDAO()).getAllZones() as ArrayList<Zone>
        } else {
            zoneRepository.getAllZones() as ArrayList<Zone>
        }

        val location = LocationMapper().toLocation(locationMapper)

        GlobalLogger.shared.info(context, "ZonesResult is $zonesResult")
        GlobalLogger.shared.info(context, "Location is $location")

        ZoneManager(context, zonesResult).dispatchZonesAndNotification(location)
    }
}