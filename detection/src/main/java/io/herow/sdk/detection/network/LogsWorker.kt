package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder

class LogsWorker(context: Context,
                 workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_PLATFORM = "detection.platform"
        const val KEY_LOGS  = "detection.logs"
    }
    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val platform = getPlatform()
        val herowAPI: HerowAPI = RetrofitBuilder.buildRetrofitForAPI(sessionHolder, getApiUrl(platform), HerowAPI::class.java)
        launchLogsRequest(herowAPI)
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

    private suspend fun launchLogsRequest(herowAPI: HerowAPI) {
        val logs = extractLogs()
        if (logs.isNotEmpty()) {
            val logsResponse = herowAPI.logs(logs)
            if (logsResponse.isSuccessful) {
                println("Request has been sent")
            } else {
                println(logsResponse)
            }
        }
    }

    private fun extractLogs(): String {
        val logs = inputData.getString(KEY_LOGS) ?: ""
        if (logs.isNotEmpty()) {
            return logs
        }
        return ""
    }
}