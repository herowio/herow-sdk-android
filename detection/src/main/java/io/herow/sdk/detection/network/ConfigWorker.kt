package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.detection.helpers.DateHelper

/**
 * @see IHerowAPI#config()
 */
class ConfigWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        GlobalLogger.shared.info(applicationContext, "DoWork is called")

        val authRequest = AuthRequests(sessionHolder, inputData)
        authRequest.execute {
            GlobalLogger.shared.info(applicationContext, "Launching configRequest")
            launchConfigRequest(sessionHolder, authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchConfigRequest(sessionHolder: SessionHolder, herowAPI: IHerowAPI) {
        GlobalLogger.shared.info(
            applicationContext,
            "Should launch: ${shouldLaunchConfigRequest(sessionHolder)}"
        )
        val configResponse = herowAPI.config()
        GlobalLogger.shared.info(
            applicationContext,
            "Thread in launchConfig is: ${Thread.currentThread().name}"
        )
        GlobalLogger.shared.info(applicationContext, "ConfigResponse: $configResponse")
        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                GlobalLogger.shared.info(applicationContext, "ConfigResponse is successful")

                ConfigDispatcher.dispatchConfigResult(configResult)
                GlobalLogger.shared.info(applicationContext, "Dispatcher method has been called")

                sessionHolder.saveRepeatInterval(configResult.configInterval)
                sessionHolder.saveConfig(configResult)

                val headers = configResponse.headers()
                GlobalLogger.shared.info(context = null, "Headers in ConfigWorker are: $headers")
                headers[HerowHeaders.LAST_TIME_CACHE_MODIFIED]?.let { remoteCachedTime: String ->
                    defineCacheStatus(
                        sessionHolder,
                        remoteCachedTime
                    )
                }

                GlobalLogger.shared.info(context = null, "Headers are: $headers")
            }

            sessionHolder.saveConfigLaunch(TimeHelper.getCurrentTime())
        }
    }

    /**
     * Check if cache time has already been saved into SP
     */
    private fun defineCacheStatus(
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
            DateHelper.convertStringToTimeStampInMilliSeconds(sessionHolder.getLastSavedModifiedDateTimeCache())
        val remoteCachedTimeToLong =
            DateHelper.convertStringToTimeStampInMilliSeconds(remoteCachedTime)

        GlobalLogger.shared.info(
            null,
            "Remote cache $remoteCachedTimeToLong && Saved time $savedTimeStamp"
        )

        return remoteCachedTimeToLong > savedTimeStamp
    }

    /**
     * To avoid lauching Config request too early
     * we need to make sure the repeat interval value is respected
     */
    private fun shouldLaunchConfigRequest(sessionHolder: SessionHolder): Boolean {
        if (sessionHolder.firstTimeLaunchingConfig()) {
            return true
        }

        val lastTimeLaunched =
            sessionHolder.getLastConfigLaunch() + sessionHolder.getRepeatInterval()
        val currentTime = TimeHelper.getCurrentTime()

        if (currentTime > lastTimeLaunched) {
            return true
        }

        return false
    }
}