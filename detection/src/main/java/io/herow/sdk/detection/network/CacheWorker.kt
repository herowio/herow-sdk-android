package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.*
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.CacheDispatcher

/**
 * Allow use to receive the zones to monitor and the pois to add in the HerowLogContext from the
 * Herow platform. You need to use a geohash to call the corresponding API.
 */
class CacheWorker(context: Context,
                  workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_GEOHASH = "detection.geohash"
    }

    private lateinit var geoHash: String

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))

        val authRequest = AuthRequests(sessionHolder, inputData)

        authRequest.execute {
            launchCacheRequest(sessionHolder, authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchCacheRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        if (sessionHolder.getUpdateCacheStatus()) {
            geoHash = extractGeoHash()
            if (geoHash.isNotEmpty()) {
                val cacheResponse = herowAPI.cache(geoHash.substring(0, 4))
                if (cacheResponse.isSuccessful) {
                    cacheResponse.body()?.let { cacheResult: CacheResult ->
                        CacheDispatcher.dispatchCacheResult(cacheResult)
                    }
                }
            }
        }
    }

    private fun extractGeoHash(): String {
        val geoHashExtracted = inputData.getString(KEY_GEOHASH) ?: ""
        if (geoHashExtracted.isNotEmpty()) {
            return geoHashExtracted
        }
        return ""
    }

}