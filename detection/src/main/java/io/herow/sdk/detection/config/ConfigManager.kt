package io.herow.sdk.detection.config

import android.content.Context
import androidx.work.*
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.ConfigWorker
import io.herow.sdk.detection.network.NetworkWorkerTags

class ConfigManager(val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Launch the necessary requests to configure the SDK & thus launch the geofencing monitoring.
     * Interval is by default 15 minutes
     */
    private fun launchConfigRequest(sessionHolder: SessionHolder) {
        val workOfData = WorkHelper.getWorkOfData(sessionHolder)
        val platform = WorkHelper.getPlatform(sessionHolder)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        GlobalLogger.shared.info(context, "LaunchConfigRequest method is called")

        val configWorkRequest = OneTimeWorkRequestBuilder<ConfigWorker>()
            .addTag(NetworkWorkerTags.CONFIG)
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to workOfData[Constants.SDK_ID],
                    AuthRequests.KEY_SDK_KEY to workOfData[Constants.SDK_KEY],
                    AuthRequests.KEY_CUSTOM_ID to workOfData[Constants.CUSTOM_ID],
                    AuthRequests.KEY_PLATFORM to platform[Constants.PLATFORM]!!.name
                )
            )
            .build()
        workManager.enqueue(configWorkRequest)
        GlobalLogger.shared.info(context, "Config request is enqueued")
    }

    private fun shouldLaunchConfigWorker(sessionHolder: SessionHolder): Boolean {
        if (sessionHolder.firstTimeLaunchingConfig()) {
            return true
        }

        val lastConfigLaunchTimestamp = sessionHolder.getLastConfigLaunch()
        val currentTimestamp = TimeHelper.getCurrentTime()

        val repeatInterval: Long = if (sessionHolder.hasNoRepeatIntervalSaved()) {
            GlobalLogger.shared.info(context, "No repeatInterval has been saved yet")
            900000
        } else {
            val savedRepeat = sessionHolder.getRepeatInterval()
            GlobalLogger.shared.info(context, "RepeatInterval previously saved is $savedRepeat")
            if (savedRepeat < 900000) {
                900000
            } else {
                savedRepeat
            }
        }

        return lastConfigLaunchTimestamp.plus(repeatInterval) > currentTimestamp
    }

    fun checkConfig(sessionHolder: SessionHolder) {
        if (shouldLaunchConfigWorker(sessionHolder)) {
            GlobalLogger.shared.info(context, "Config request should be launched")
            launchConfigRequest(sessionHolder)
        } else {
            GlobalLogger.shared.info(context, "Dispatching saved config but not fetching it")
            val configResult = sessionHolder.getConfigResult()
            if (configResult != null) {
                ConfigDispatcher.dispatchConfigResult(configResult)
            }
        }
    }
}