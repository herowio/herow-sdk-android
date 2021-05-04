package io.herow.sdk.detection.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.analytics.ApplicationData
import io.herow.sdk.detection.analytics.LogsDispatcher
import io.herow.sdk.detection.analytics.model.LogRedirect

class NotificationReceiver : BroadcastReceiver() {

    // This event is called when user clicks on a notification
    // We dispatch a Log with subtype REDIRECT whenever someone clicks on a notification
    override fun onReceive(context: Context?, intent: Intent) {
        GlobalLogger.shared.info(context, "Data received from notification: $intent")

        val applicationData = ApplicationData(context!!)
        val sessionHolder = SessionHolder(DataHolder(context))

        val hashZone: String? = intent.getStringExtra(NotificationManager.ID_ZONE)
        val idCampaign: String? = intent.getStringExtra(NotificationManager.ID_CAMPAIGN)

        val redirectLog = LogRedirect(hashZone!!, idCampaign!!)
        redirectLog.enrich(applicationData, sessionHolder)

        LogsDispatcher.dispatchLogsResult(arrayListOf(Log(redirectLog)))
        GlobalLogger.shared.info(context, "Dispatching Log created when Notification is clicked")
    }
}