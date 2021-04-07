package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.detection.helpers.DateHelper

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
            GlobalLogger.shared.info(applicationContext,  "Launching configRequest")
            launchConfigRequest(sessionHolder, authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchConfigRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        val configResponse = herowAPI.config()
        GlobalLogger.shared.info(applicationContext,  "ConfigResponse: $configResponse")

        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                GlobalLogger.shared.info(applicationContext, "ConfigResponse is successful")

                ConfigDispatcher.dispatchConfigResult(configResult)
                GlobalLogger.shared.info(applicationContext,  "Dispatcher method has been called")

                sessionHolder.saveRepeatInterval(configResult.configInterval)

                val headers = configResponse.headers()
                headers[HerowHeaders.LAST_TIME_CACHE_MODIFIED]?.let { remoteCachedTime: String ->
                    checkCacheState(
                        sessionHolder,
                        remoteCachedTime
                    )
                }
            }
        }
    }

    /**
     * Check if cache time has already been saved into SP
     */
    private fun checkCacheState(
        sessionHolder: SessionHolder,
        remoteCachedTime: String
    ) {
        if (!sessionHolder.hasNoCacheTimeSaved()) {
            if (shouldCacheBeUpdated(remoteCachedTime, sessionHolder)) {
                sessionHolder.updateCache(true)
            } else {
                sessionHolder.updateCache(false)
            }
        } else {
            sessionHolder.updateCache(true)
        }

        sessionHolder.saveModifiedCacheTime(remoteCachedTime)
    }

    /**
     * Both saved cache time & remote cache time are converted to Timestamp
     * in order to compare them
     */
    private fun shouldCacheBeUpdated(
        remoteCachedTime: String,
        sessionHolder: SessionHolder
    ): Boolean {
        val savedTimeStamp =
            DateHelper.convertStringToTimeStamp(sessionHolder.getLastSavedModifiedDateTimeCache())
        val remoteCachedTimeToLong = DateHelper.convertStringToTimeStamp(remoteCachedTime)

        return (remoteCachedTimeToLong > savedTimeStamp)
    }
}