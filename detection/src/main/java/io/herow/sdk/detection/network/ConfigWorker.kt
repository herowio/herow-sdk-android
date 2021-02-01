package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.detection.HerowInitializer

/**
 * @see HerowAPI#config()
 */
class ConfigWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))

        val authRequest = AuthRequests(sessionHolder, inputData)
        authRequest.execute {
            launchConfigRequest(sessionHolder, authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchConfigRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        val configResponse = herowAPI.config()
        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                ConfigDispatcher.dispatchConfigResult(configResult)

                val headers = configResponse.headers()
                headers[HerowHeaders.LAST_TIME_CACHE_MODIFIED]?.let { lastTimeCacheWasModified: String ->
                    checkCacheState(sessionHolder, lastTimeCacheWasModified.toLong())
                    println(lastTimeCacheWasModified.toLong())
                }
            }
        }
    }

    private fun checkCacheState(sessionHolder: SessionHolder, lastTimeCacheWasModified: Long) {
        if (sessionHolder.isCacheTimeSaved()) {
            if (sessionHolder.shouldCacheBeUpdated(lastTimeCacheWasModified))
                sessionHolder.updateCache(true)
        } else {
            sessionHolder.updateCache(false)
        }

        sessionHolder.saveModifiedCacheTime(lastTimeCacheWasModified)
    }

    private fun checkIntervalState(sessionHolder: SessionHolder, interval: Long) {
    }
}