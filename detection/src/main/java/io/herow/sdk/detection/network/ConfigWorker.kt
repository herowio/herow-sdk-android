package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.*
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.validator.AuthRequests

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
            launchConfigRequest(authRequest.getHerowAPI())
        }

        return Result.success()
    }

    private suspend fun launchConfigRequest(herowAPI: HerowAPI) {
        val configResponse = herowAPI.config()
        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                if (configResult.isGeofenceEnable) {
                    HerowInitializer.launchGeofencingMonitoring()
                }
                val lastTimeCacheWasModified =
                    configResponse.headers()[HerowHeaders.LAST_TIME_CACHE_MODIFIED]
                println(lastTimeCacheWasModified)
            }
        }
    }
}