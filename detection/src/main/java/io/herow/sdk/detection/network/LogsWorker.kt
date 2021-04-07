package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.SessionHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogsWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        const val KEY_LOGS = "detection.logs"
        var logsWorkerHashMap = HashMap<String?, String?>()
        var workerID: String = "workerID"
    }

    private lateinit var sessionHolder: SessionHolder
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun doWork(): Result {
        sessionHolder = SessionHolder(DataHolder(applicationContext))

        val autRequest = AuthRequests(sessionHolder, inputData)
        if (!sessionHolder.getOptinValue()) {
            GlobalLogger.shared.debug(context, "Optin value is set to false")
            return Result.failure()
        }

        autRequest.execute {
            withContext(dispatcher) {
                launchLogsRequest(autRequest.getHerowAPI())
            }
        }
        return Result.success()
    }

    private suspend fun launchLogsRequest(herowAPI: HerowAPI) {
        val log = logsWorkerHashMap[inputData.getString(workerID)]
        GlobalLogger.shared.info(context,  "Log to send is: $log")
        GlobalLogger.shared.info(context,  "LogWorkerID is: ${inputData.getString(workerID)}")

        if (!log.isNullOrEmpty()) {
            val logResponse = herowAPI.log(log)
            GlobalLogger.shared.info(context,  "LogResponse is: $logResponse")

            if (logResponse.isSuccessful) {
                logsWorkerHashMap.remove(workerID)
                GlobalLogger.shared.info(context, "Log has been sent")
                println("Request has been sent")
            } else {
                println(logResponse)
            }
        }
    }
}