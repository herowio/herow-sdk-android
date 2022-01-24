package io.herow.sdk.detection.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.detection.analytics.ApplicationData
import io.herow.sdk.detection.analytics.LogsDispatcher
import io.herow.sdk.detection.analytics.model.LogRedirect
import io.herow.sdk.detection.koin.ICustomKoinComponent
import org.koin.core.component.inject

class NotificationReceiver : BroadcastReceiver(), ICustomKoinComponent {

    private val sessionHolder: SessionHolder by inject()

    /** This event is called when user clicks on a notification
     * We dispatch a Log with subtype REDIRECT whenever someone clicks on a notification **/
    override fun onReceive(context: Context?, intent: Intent) {
        GlobalLogger.shared.info(context, "Data received from notification: $intent")

        val applicationData = ApplicationData(context!!)

        val hashZone: String? = intent.getStringExtra(NotificationManager.ID_ZONE)
        val idCampaign: String? = intent.getStringExtra(NotificationManager.ID_CAMPAIGN)

        launchDeepLinkIntent(intent, context)

        val redirectLog = LogRedirect(hashZone!!, idCampaign!!)
        redirectLog.enrich(applicationData, sessionHolder)

        LogsDispatcher.dispatchLogsResult(arrayListOf(Log(redirectLog)))
        GlobalLogger.shared.info(context, "Dispatching Log created when Notification is clicked")
    }

    private fun launchDeepLinkIntent(intent: Intent, context: Context) {
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, intent.data)
        deepLinkIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val component = deepLinkIntent.resolveActivity(context.packageManager)

        component?.let { context.startActivity(deepLinkIntent) } ?: run {
            android.util.Log.i(
                "XXX",
                "No application can handle the link"
            )
        }
    }
}