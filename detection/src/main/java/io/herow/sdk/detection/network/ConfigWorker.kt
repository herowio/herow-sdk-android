package io.herow.sdk.detection.network

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.detection.helpers.DateHelper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * @see IHerowAPI#config()
 */
@Keep
class ConfigWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), ICustomKoinComponent {

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val sessionHolder: SessionHolder by inject()
    var testing = false

    override suspend fun doWork(): Result {
        val authRequest = AuthRequests(inputData)

        applicationScope.launch {
            authRequest.execute {
                try {
                     launchConfigRequest(authRequest.getHerowAPI())
                } catch (exception: Throwable) {
                    println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
                }
            }
        }

        return Result.success()
    }

    private suspend fun launchConfigRequest(herowAPI: IHerowAPI) {
        GlobalLogger.shared.info(applicationContext, "Should launch: ${shouldLaunchConfigRequest()}")

        val configResponse = herowAPI.config()
        GlobalLogger.shared.info(applicationContext, "ConfigResponse: $configResponse")

        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                ConfigDispatcher.dispatchConfigResult(configResult)

                sessionHolder.saveRepeatInterval(configResult.configInterval)
                sessionHolder.saveConfig(configResult)

                val headers = configResponse.headers()
                GlobalLogger.shared.info(context = null, "Headers in ConfigWorker are: $headers")

                headers[HerowHeaders.LAST_TIME_CACHE_MODIFIED]?.let { remoteCachedTime: String ->
                    defineCacheStatus(remoteCachedTime)
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
        remoteCachedTime: String
    ) {
        if (!sessionHolder.hasNoCacheTimeSaved()) {
            if (shouldCacheBeUpdated(remoteCachedTime)) {
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
        remoteCachedTime: String
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
    private fun shouldLaunchConfigRequest(): Boolean {
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