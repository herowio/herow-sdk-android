package io.herow.sdk.detection.analytics

import android.content.Context
import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.location.LocationDispatcher

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

                HerowInitializer.getInstance(context).launchLogsRequest(logJsonString)
            }

        }
    }
}