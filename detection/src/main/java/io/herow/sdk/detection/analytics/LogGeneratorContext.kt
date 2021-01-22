package io.herow.sdk.detection.analytics

import android.location.Location
import com.google.gson.GsonBuilder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.states.app.AppStateListener
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.analytics.adapter.LocationAdapter
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.location.LocationListener

class LogGeneratorContext(private val applicationData: ApplicationData,
                          private val sessionHolder: SessionHolder): AppStateListener, LocationListener {
    private var appState: String = "bg"

    override fun onAppInForeground() {
        appState = "fg"
    }

    override fun onAppInBackground() {
        appState = "bg"
    }

    override fun onLocationUpdate(location: Location) {
        sendLog(location)
    }

    private fun sendLog(location: Location) {
        val herowLogContext = HerowLogContext(appState, location)
        herowLogContext.enrich(applicationData, sessionHolder)
        val listOfLogs = listOf(Log(herowLogContext, TimeHelper.getCurrentTime()))
        val gson = GsonBuilder()
            .registerTypeAdapter(Location::class.java, LocationAdapter())
            .create()
        val logs = Logs(listOfLogs)
        val logJsonString: String = gson.toJson(logs, Logs::class.java)
        HerowInitializer.launchLogsRequest(logJsonString)
    }
}