package io.herow.sdk.detection.network

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.koin.ICustomKoinComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.inject

@Keep
class LogsWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), ICustomKoinComponent {

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val sessionHolder: SessionHolder by inject()
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    var testing = false

    companion object {
        const val KEY_LOGS = "detection.logs"
        var logsWorkerHashMap = HashMap<String?, String?>()
        var workerID: String = "workerID"
    }

    override suspend fun doWork(): Result {
        val authRequests = AuthRequests(inputData)
        if (!sessionHolder.getOptinValue()) {
            GlobalLogger.shared.debug(context, "Optin value is set to false")

            return Result.failure()
        }

        applicationScope.launch {
            authRequests.execute {
                try {
                    launchLogsRequest(authRequests.getHerowAPI())
                } catch (exception: Throwable) {
                    println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
                }
            }
        }

        return Result.success()
    }

    private suspend fun launchLogsRequest(herowAPI: IHerowAPI) {
        val log = logsWorkerHashMap[inputData.getString(workerID)]
        GlobalLogger.shared.info(context, "Log to send is: $log")

        if (!log.isNullOrEmpty()) {
            val logResponse = herowAPI.log(log)
            GlobalLogger.shared.info(context, "LogResponse is: $logResponse")

            if (logResponse.isSuccessful) {
                logsWorkerHashMap.remove(workerID)
                GlobalLogger.shared.info(context, "Log has been sent")
            } else {
                println(logResponse)
            }
        }
    }
}