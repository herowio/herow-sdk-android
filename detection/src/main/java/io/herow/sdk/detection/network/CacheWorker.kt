package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.*
import io.herow.sdk.connection.cache.CacheResult
import io.herow.sdk.detection.cache.CacheDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

class CacheWorker(context: Context,
                  workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_PLATFORM = "detection.platform"
        const val KEY_GEOHASH  = "detection.geohash"
    }
    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val platform = getPlatform()
        val herowAPI: HerowAPI = RetrofitBuilder.buildRetrofitForAPI(sessionHolder, getApiUrl(platform), HerowAPI::class.java)
        launchCacheRequest(herowAPI)
        return Result.success()
    }

    private fun getPlatform(): HerowPlatform {
        val platformURLString = inputData.getString(KEY_PLATFORM) ?: ""
        if (platformURLString.isNotEmpty()) {
            if (HerowPlatform.PRE_PROD == HerowPlatform.valueOf(platformURLString)) {
                return HerowPlatform.PRE_PROD
            }
        }
        return HerowPlatform.PROD
    }

    private fun getApiUrl(platform: HerowPlatform): String {
        if (platform == HerowPlatform.PRE_PROD) {
            return HerowAPI.PRE_PROD_BASE_URL
        }
        return HerowAPI.PROD_BASE_URL
    }

    private suspend fun launchCacheRequest(herowAPI: HerowAPI) {
        val geoHash = extractGeoHash()
        if (geoHash.isNotEmpty()) {
            val cacheResponse = herowAPI.cache(geoHash.substring(0,4))
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