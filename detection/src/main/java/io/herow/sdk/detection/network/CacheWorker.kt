package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.helpers.DefaultDispatcherProvider
import io.herow.sdk.detection.helpers.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Allow user to receive the zones to monitor and the pois to add in the HerowLogContext from the
 * Herow platform. You need to use a geohash to call the corresponding API.
 */
class CacheWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_GEOHASH = "detection.geohash"
        private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
    }

    private var inputGeoHash = inputData.getString(KEY_GEOHASH) ?: ""
    private var db: HerowDatabase = HerowDatabase.getDatabase(context)

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val authRequest = AuthRequests(sessionHolder, inputData)

        authRequest.execute {
            launchCacheRequest(sessionHolder, authRequest.getHerowAPI())
        }

        return Result.success()
    }

    /**
     * If GeoHash is unknown or different from the saved one, cache is updated
     * If cache interval has been reached, cache is updated
     */
    private suspend fun launchCacheRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        if (sessionHolder.getUpdateCacheStatus() || isGeoHashUnknownOrDifferent(sessionHolder)) {
            val extractedGeoHash = extractGeoHash(sessionHolder)

            if (extractedGeoHash.isNotEmpty()) {
                val cacheResponse = herowAPI.cache(extractedGeoHash.substring(0, 4))
                if (cacheResponse.isSuccessful) {
                    cacheResponse.body()?.let { cacheResult: CacheResult ->
                        val job = CoroutineScope(dispatcher.io()).launch {
                            clearDatabase()
                            saveCacheDataInDB(cacheResult)
                        }
                        job.join()

                        CacheDispatcher.dispatch()
                    }
                }
                db.close()
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

    private fun clearDatabase() = db.clearAllTables()

    private fun saveCacheDataInDB(cacheResult: CacheResult) {
        val zoneRepository = ZoneRepository(db.zoneDAO())
        val poiRepository = PoiRepository(db.poiDAO())
        val campaignRepository = CampaignRepository(db.campaignDAO())

        db.runInTransaction {
            for (zone in cacheResult.zones) {
                zoneRepository.insert(zone)
            }

            for (poi in cacheResult.pois) {
                poiRepository.insert(poi)
            }

            for (campaign in cacheResult.campaigns) {
                campaignRepository.insert(campaign)
            }
        }
    }
}