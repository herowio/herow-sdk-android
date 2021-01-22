package io.herow.sdk.detection.analytics

import android.location.Location
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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
    private var isOnForeground: Boolean = false

    override fun onAppInForeground() {
        isOnForeground = true
    }

    override fun onAppInBackground() {
        isOnForeground = false
    }

    override fun onLocationUpdate(location: Location) {
        sendLog(location)
    }

    private fun sendLog(location: Location) {
        val herowLogContext = HerowLogContext(isOnForeground, location)
        herowLogContext.enrich(applicationData, sessionHolder)
        val listOfLogs = listOf(Log(herowLogContext, TimeHelper.getCurrentTime()))
        val moshi = Moshi.Builder()
            .add(LocationAdapter())
            .build()
        val logs = Logs(listOfLogs)
        val logAdapter: JsonAdapter<Logs> = moshi.adapter(Logs::class.java)
        val logJsonString = logAdapter.toJson(logs)
        HerowInitializer.launchLogsRequest(logJsonString)
    }
}