package io.herow.sdk.detection.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.SessionHolder

class LogsWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_LOGS = "detection.logs"
    }

    private lateinit var sessionHolder: SessionHolder

    override suspend fun doWork(): Result {
        sessionHolder = SessionHolder(DataHolder(applicationContext))

        val autRequest = AuthRequests(sessionHolder, inputData)
        if (!sessionHolder.getOptinValue()) {
            Log.d("Optin", "Optin value is set to false")
            return Result.failure()
        }

        autRequest.execute {
            launchLogsRequest(autRequest.getHerowAPI())
        }
        return Result.success()
    }

    private suspend fun launchLogsRequest(herowAPI: HerowAPI) {
        val logs = extractLogs()
        if (logs.isNotEmpty()) {
            val logsResponse = herowAPI.log(logs)
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