package io.herow.sdk.detection.analytics

import android.content.Context
import android.util.Log.i
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.location.LocationDispatcher

class LogsManager(private val context: Context): LogsListener {
    private val applicationData = ApplicationData(context)
    private val sessionHolder = SessionHolder(DataHolder(context))
    private val logGeneratorEvent = LogGeneratorEvent(applicationData, sessionHolder, context)

    init {
        AppStateDetector.addAppStateListener(logGeneratorEvent)
        LocationDispatcher.addLocationListener(logGeneratorEvent)
        CacheDispatcher.addCacheListener(logGeneratorEvent)
        GeofenceDispatcher.addGeofenceListener(logGeneratorEvent)
    }

    //TODO Extract each Log
    override fun onLogsToSend(listOfLogs: List<Log>) {
        if (listOfLogs.isNotEmpty()) {
            i("XXX/EVENT", "LogsManager - ListOfLogs is not empty: $listOfLogs")
            //val logs = Logs(listOfLogs)

            for (log in listOfLogs) {
                val logJsonString: String = GsonProvider.toJson(log, Log::class.java)

                i("XXX/EVENT", "LogsManager - Log one by one: $logJsonString")
                HerowInitializer.getInstance(context).launchLogsRequest(logJsonString)
            }

        }
    }
}