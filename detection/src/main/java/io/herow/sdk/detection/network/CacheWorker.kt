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
class CacheWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_GEOHASH = "detection.geohash"
    }

    private var inputGeoHash = inputData.getString(KEY_GEOHASH) ?: ""

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))

        val authRequest = AuthRequests(sessionHolder, inputData)

        authRequest.execute {
            launchCacheRequest(sessionHolder, authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchCacheRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        if (sessionHolder.getUpdateCacheStatus() || geoHashIsUnknownOrDifferent(sessionHolder)) {
            val extractedGeoHash = extractGeoHash(sessionHolder)

            if (extractedGeoHash.isNotEmpty()) {
                val cacheResponse = herowAPI.cache(extractedGeoHash.substring(0, 4))
                if (cacheResponse.isSuccessful) {
                    cacheResponse.body()?.let { cacheResult: CacheResult ->
                        CacheDispatcher.dispatchCacheResult(cacheResult)
                    }
                }
            }
        }
    }

    private fun geoHashIsUnknownOrDifferent(sessionHolder: SessionHolder): Boolean {
        if (sessionHolder.hasNoGeoHashSaved()) {
            return true
        } else {
            if (inputGeoHash.isEmpty() || inputGeoHash != sessionHolder.getGeohash()) {
                return true
            }
        }

        return false
    }

    private fun extractGeoHash(sessionHolder: SessionHolder): String {
        if (inputGeoHash.isNotEmpty()) {
            sessionHolder.saveGeohash(inputGeoHash)
            return inputGeoHash
        }

        return ""
    }
}