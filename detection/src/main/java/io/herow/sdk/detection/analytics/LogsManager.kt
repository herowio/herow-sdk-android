package io.herow.sdk.detection.analytics

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.location.LocationDispatcher
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.LogsWorker
import io.herow.sdk.detection.network.NetworkWorkerTags
import java.util.*

class LogsManager(private val context: Context) : LogsListener {
    private val applicationData = ApplicationData(context)
    private val sessionHolder = SessionHolder(DataHolder(context))
    private val logGeneratorEvent = LogGeneratorEvent(applicationData, sessionHolder, context)

    init {
        AppStateDetector.addAppStateListener(logGeneratorEvent)
        LocationDispatcher.addLocationListener(logGeneratorEvent)
        CacheDispatcher.addCacheListener(logGeneratorEvent)
        GeofenceDispatcher.addGeofenceListener(logGeneratorEvent)
    }

    /**
     * Expose annotation isn't considered by default
     * Cf https://stackoverflow.com/questions/43047823/gson-parameter-get-serialised-even-though-it-has-exposeserialize-false
     */
    override fun onLogsToSend(listOfLogs: List<Log>) {
        if (listOfLogs.isNotEmpty()) {
            GlobalLogger.shared.info(context, "List of logs is: $listOfLogs")

            for (log in listOfLogs) {
                val logJsonString: String = Gson().toJson(log, Log::class.java)
                GlobalLogger.shared.info(context, "Log one by one: $logJsonString")

                launchLogsRequest(logJsonString)
            }
        }
    }

    /**
     * Launch the logs request to send the events to he Herow Platform
     */
    private fun launchLogsRequest(log: String) {
        val workManager = WorkManager.getInstance(context)
        val uuid = UUID.randomUUID().toString()

        LogsWorker.logsWorkerHashMap[uuid] = log
        GlobalLogger.shared.info(context, "CurrentID is $uuid")
        GlobalLogger.shared.info(context, "LaunchLogsRequest method is called")

        val workOfData = WorkHelper.getWorkOfData(sessionHolder)
        val platform = WorkHelper.getPlatform(sessionHolder)
        GlobalLogger.shared.debug(
            context,
            "Is work scheduled: ${
                WorkHelper.isWorkNotScheduled(
                    workManager,
                    NetworkWorkerTags.LOGS
                )
            }"
        )
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<LogsWorker>()
            .addTag(NetworkWorkerTags.LOGS)
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to workOfData[Constants.SDK_ID],
                    AuthRequests.KEY_SDK_KEY to workOfData[Constants.SDK_KEY],
                    AuthRequests.KEY_CUSTOM_ID to workOfData[Constants.CUSTOM_ID],
                    AuthRequests.KEY_PLATFORM to platform[Constants.PLATFORM]!!.name,
                    LogsWorker.workerID to uuid
                )
            )
            .build()
        workManager.enqueue(workerRequest)
        GlobalLogger.shared.info(context, "Log request is enqueued")
    }
}