package io.herow.sdk.detection.analytics

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.analytics.model.HerowLogEnter
import io.herow.sdk.detection.geofencing.GeofenceEvent
import io.herow.sdk.detection.geofencing.GeofenceListener

class LogGeneratorEnter(private val applicationData: ApplicationData,
                        private val sessionHolder: SessionHolder): AppStateListener, GeofenceListener {
    private var appState: String = "bg"

    override fun onAppInForeground() {
        appState = "fg"
    }

    override fun onAppInBackground() {
        appState = "bg"
    }

    override fun onGeofenceEvent(geofenceEvents: List<GeofenceEvent>) {
        sendLog(geofenceEvents)
    }

    private fun sendLog(geofenceEvents: List<GeofenceEvent>) {
        val listOfLogs = ArrayList<Log>()
        for (geofenceEvent in geofenceEvents) {
            val herowLogEnter = HerowLogEnter(appState, geofenceEvent)
            herowLogEnter.enrich(applicationData, sessionHolder)
            listOfLogs.add(Log(herowLogEnter, TimeHelper.getCurrentTime()))
        }

        val logs = Logs(listOfLogs)
        val logJsonString: String = GsonProvider.toJson(logs, Logs::class.java)
        HerowInitializer.launchLogsRequest(logJsonString)
    }
}