package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Allow user to receive the zones to monitor and the pois to add in the HerowLogContext from the
 * Herow platform. You need to use a geohash to call the corresponding API.
 */
class CacheWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private lateinit var zoneRepository: ZoneRepository
    private lateinit var poiRepository: PoiRepository
    private lateinit var campaignRepository: CampaignRepository
    private var inputGeoHash = inputData.getString(KEY_GEOHASH) ?: ""

    companion object {
        const val KEY_GEOHASH = "detection.geohash"
    }

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val authRequest = AuthRequests(sessionHolder, inputData)
        val database: HerowDatabase = HerowDatabase.getDatabase(applicationContext)

        zoneRepository = ZoneRepository(database.zoneDAO())
        poiRepository = PoiRepository(database.poiDAO())
        campaignRepository = CampaignRepository(database.campaignDAO())

        if (!sessionHolder.getOptinValue()) {
            GlobalLogger.shared.info(context,"Optin value is set to false")
            return Result.failure()
        }

        authRequest.execute {
            withContext(ioDispatcher) {
                GlobalLogger.shared.info(context,"Launching cacheRequest")
                launchCacheRequest(sessionHolder, authRequest.getHerowAPI(), database)
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
        database: HerowDatabase
    ) {
        if (sessionHolder.getUpdateCacheStatus() || isGeoHashUnknownOrDifferent(sessionHolder)) {
            val extractedGeoHash = extractGeoHash(sessionHolder)
            if (extractedGeoHash.isNotEmpty()) {
                val cacheResponse = herowAPI.cache(extractedGeoHash.substring(0, 4))
                GlobalLogger.shared.info(context,"Cache response is $cacheResponse")

                if (cacheResponse.isSuccessful) {
                    GlobalLogger.shared.info(context,"CacheResponse is successful")
                    cacheResponse.body()?.let { cacheResult: CacheResult? ->
                        GlobalLogger.shared.info(context,"CacheResult is $cacheResult")
                        withContext(ioDispatcher) {
                            try {
                                database.clearAllTables()
                                GlobalLogger.shared.info(context,"Database has been cleared")

                                for (zone in cacheResult!!.zones) {
                                    GlobalLogger.shared.info(context, "Zone is: $zone")
                                }

                                saveCacheDataInDB(cacheResult)
                                GlobalLogger.shared.info(context,"CacheResult has been saved in BDD")

                                CacheDispatcher.dispatch()
                                GlobalLogger.shared.info(context,"Dispatching zones")
                            } catch (e: Exception) {
                                GlobalLogger.shared.error(context,"Exception catched is: $e")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * First check if GeoHash has already been saved into SP
     * Then check if GeoHash is empty or different from saved data
     */
    private fun isGeoHashUnknownOrDifferent(sessionHolder: SessionHolder): Boolean {
        if (sessionHolder.hasNoGeoHashSaved()) {
            return true
        } else {
            if (inputGeoHash.isEmpty() || inputGeoHash != sessionHolder.getGeohash()) {
                return true
            }
        }

        return false
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
        if (!cacheResult.zones.isNullOrEmpty()) {
            for (zone in cacheResult.zones) {
                zoneRepository.insert(zone)
            }
            GlobalLogger.shared.info(context,"Zones have been saved in DB")
        }

        if (!cacheResult.pois.isNullOrEmpty()) {
            for (poi in cacheResult.pois) {
                poiRepository.insert(poi)
            }
            GlobalLogger.shared.info(context,"Pois have been saved in DB")
        }

        if (!cacheResult.campaigns.isNullOrEmpty()) {
            for (campaign in cacheResult.campaigns) {
                campaignRepository.insert(campaign)
            }
            GlobalLogger.shared.info(context,"Campaigns have been saved in DB")
        }
    }
}