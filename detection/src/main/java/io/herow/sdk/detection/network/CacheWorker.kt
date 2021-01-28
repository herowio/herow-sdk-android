package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.*
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.CacheDispatcher

class CacheWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_GEOHASH = "detection.geohash"
    }

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))

        val authRequest = AuthRequests(sessionHolder, inputData)
        authRequest.execute {
            launchCacheRequest(authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchCacheRequest(herowAPI: HerowAPI) {
        val geoHash = extractGeoHash()
        if (geoHash.isNotEmpty()) {
            val cacheResponse = herowAPI.cache(geoHash.substring(0, 4))
            if (cacheResponse.isSuccessful) {
                cacheResponse.body()?.let { cacheResult: CacheResult ->
                    CacheDispatcher.dispatchCacheResult(cacheResult)
                }
            }
        }
    }

    private fun extractGeoHash(): String {
        val geoHash = inputData.getString(KEY_GEOHASH) ?: ""
        if (geoHash.isNotEmpty()) {
            return geoHash
        }
        return ""
    }
}