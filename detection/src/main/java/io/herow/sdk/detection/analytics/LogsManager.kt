package io.herow.sdk.detection.analytics

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.location.LocationDispatcher
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.LogsWorker
import io.herow.sdk.detection.network.NetworkWorkerTags
import org.koin.core.component.inject
import java.util.*

class LogsManager(private val context: Context) : ILogsListener, ICustomKoinComponent {
    private val applicationData = ApplicationData(context)
    private val sessionHolder: SessionHolder by inject()
    private val logGeneratorEvent = LogGeneratorEvent(applicationData, context)

    init {
        AppStateDetector.addAppStateListener(logGeneratorEvent)
        LocationDispatcher.addLocationListener(logGeneratorEvent)
        GeofenceDispatcher.addGeofenceListener(logGeneratorEvent)
    }

    /**
     * Expose annotation isn't considered by default
     * Cf https://stackoverflow.com/questions/43047823/gson-parameter-get-serialised-even-though-it-has-exposeserialize-false
     */
    override fun onLogsToSend(listOfLogs: List<Log>) {
        if (listOfLogs.isNotEmpty()) {
            GlobalLogger.shared.info(context, "List of logs is -- 5 : $listOfLogs")

            for (log in listOfLogs) {
                val logJsonString: String = Gson().toJson(log, Log::class.java)
                GlobalLogger.shared.info(context, "Log one by one: $logJsonString")

                launchLogsRequest(logJsonString)
            }
        }
    }

    /**
     * Launch the logs request to send the events to the Herow Platform
     */
    private fun launchLogsRequest(log: String) {
        val workManager = WorkManager.getInstance(context)
        val uuid = UUID.randomUUID().toString()

        LogsWorker.logsWorkerHashMap[uuid] = log
        GlobalLogger.shared.info(context, "CurrentID is $uuid")
        GlobalLogger.shared.info(context, "LaunchLogsRequest method is called")

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
                    AuthRequests.KEY_SDK_ID to sessionHolder.getSDKID(),
                    AuthRequests.KEY_SDK_KEY to sessionHolder.getSdkKey(),
                    AuthRequests.KEY_CUSTOM_ID to sessionHolder.getCustomID(),
                    if (HerowInitializer.isTesting()) {
                        AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name
                    } else {
                        AuthRequests.KEY_PLATFORM to sessionHolder.getPlatformName().name
                    },
                    LogsWorker.workerID to uuid
                )
            )
            .build()
        workManager.enqueue(workerRequest)
        GlobalLogger.shared.info(context, "Log request is enqueued -- 6")
    }
}