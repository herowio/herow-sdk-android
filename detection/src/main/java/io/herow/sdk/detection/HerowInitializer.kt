package io.herow.sdk.detection

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.common.states.motion.ActivityTransitionDetector
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.token.SdkSession

object HerowInitializer {
    private val activityTransitionDetector = ActivityTransitionDetector()
    private val appStateDetector = AppStateDetector()
    private var platform: HerowPlatform = HerowPlatform.PROD
    private lateinit var sdkSession: SdkSession
    private lateinit var workerManager: WorkManager

    fun init(context: Context): HerowInitializer {
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        activityTransitionDetector.launchTransitionMonitoring(context)
        workerManager = WorkManager.getInstance(context)
        return this
    }

    fun configApp(sdkId: String, sdkKey: String): HerowInitializer {
        sdkSession = SdkSession(sdkId, sdkKey)
        return this
    }

    fun configPlatform(platform: HerowPlatform): HerowInitializer {
        this.platform = platform
        return this
    }

    fun synchronize() {
        launchRequests()
    }

    private fun launchRequests() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<RequestWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(
                RequestWorker.KEY_SDK_ID   to sdkSession.sdkId,
                RequestWorker.KEY_SDK_KEY  to sdkSession.sdkKey,
                RequestWorker.KEY_PLATFORM to platform.name
            ))
            .build()
        workerManager.enqueue(workerRequest)
    }
}