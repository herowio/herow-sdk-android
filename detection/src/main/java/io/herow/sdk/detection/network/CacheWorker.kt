package io.herow.sdk.detection.network

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.*

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
        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    private var inputGeoHash = inputData.getString(KEY_GEOHASH) ?: ""

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val authRequest = AuthRequests(sessionHolder, inputData)
        val database: HerowDatabase = HerowDatabase.getDatabase(applicationContext)

        if (!sessionHolder.getOptinValue()) {
            Log.d("Optin", "Optin value is set to false")
            return Result.failure()
        }

        authRequest.execute {
            launchCacheRequest(sessionHolder, authRequest.getHerowAPI(), database)
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

                if (cacheResponse.isSuccessful) {
                    cacheResponse.body()?.let { cacheResult: CacheResult ->
                        val job = scope.launch {
                            database.clearAllTables()
                            println("Data has been deleted")
                            saveCacheDataInDB(database, cacheResult)
                        }
                        job.join()

                        println("I should be called after data saved")
                        CacheDispatcher.dispatch()
                    }
                }
                database.close()
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

    suspend fun saveCacheDataInDB(database: HerowDatabase, cacheResult: CacheResult) {
        val zoneRepository = ZoneRepository(database.zoneDAO())
        val poiRepository = PoiRepository(database.poiDAO())
        val campaignRepository = CampaignRepository(database.campaignDAO())

        for (zone in cacheResult.zones) {
            zoneRepository.insert(zone)
        }

        for (poi in cacheResult.pois) {
            poiRepository.insert(poi)
        }

        for (campaign in cacheResult.campaigns) {
            campaignRepository.insert(campaign)
        }

        println("Data has been saved")
    }
}