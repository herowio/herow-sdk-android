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
        var companionLog: String? = null
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

    // TODO Extract EACH log in order to send log by log
    private suspend fun launchLogsRequest(herowAPI: HerowAPI) {
        val log = extractLogs()
        if (log.isNotEmpty()) {
            val logResponse = herowAPI.log(log)
            Log.i("XXX/EVENT", "LogsWorker - LogResponse is: $logResponse")
            if (logResponse.isSuccessful) {
                Log.i("XXX/EVENT", "LogsWorker - Log has been sent")
                println("Request has been sent")
            } else {
                println(logResponse)
            }

            /* for (log in logs) {

            } */
        }
    }

    private fun extractLogs(): String {
        //val log = inputData.getString(KEY_LOGS) ?: ""
        val log = companionLog ?: ""
        if (log.isNotEmpty()) {
            return log
        }
        return ""
    }
}