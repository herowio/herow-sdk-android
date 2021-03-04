package io.herow.sdk.detection.helpers

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.analytics.model.HerowLogContext

object LogsHelper {
    fun createTestLogs(): String {
        val location = Location("tus")
        location.latitude = 42.6
        location.longitude = 2.5

        val herowLogContext = HerowLogContext("fg", location)

        val listOfLogs = listOf(Log(herowLogContext, TimeHelper.getCurrentTime()))
        val logs = Logs(listOfLogs)

        return GsonProvider.toJson(logs)
    }
}