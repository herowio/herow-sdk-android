package io.herow.sdk.detection

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.DeviceHelper
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.common.states.motion.ActivityTransitionDetector
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.token.SdkSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object HerowInitializer {
    private val activityTransitionDetector = ActivityTransitionDetector()
    private val appStateDetector = AppStateDetector()
    private var customId: String = ""
    private var platform: HerowPlatform = HerowPlatform.PROD
    private lateinit var sdkSession: SdkSession
    private lateinit var workerManager: WorkManager

    fun init(context: Context): HerowInitializer {
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        activityTransitionDetector.launchTransitionMonitoring(context)
        workerManager = WorkManager.getInstance(context)
        loadIdentifiers(context)
        return this
    }

    private fun loadIdentifiers(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val dataHolder = DataHolder(context)
            val deviceId = DeviceHelper.getDeviceId(context)
            if (deviceId.isNotEmpty()) {
                dataHolder["connection.device_id"] = deviceId
            }
            try {
                val advertiserInfo: AdvertisingIdClient.Info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                dataHolder["detection.ad_id"] = advertiserInfo.id
                dataHolder["detection.ad_status"] = !advertiserInfo.isLimitAdTrackingEnabled
                println("advertiserInfo.id: ${advertiserInfo.id}")
                println("advertiserInfo.ad_status: ${!advertiserInfo.isLimitAdTrackingEnabled}")
            } catch (e: NoClassDefFoundError) {
                println("Your application does not implement the play-services-ads library")
            }
        }
    }

    fun configApp(sdkId: String, sdkKey: String): HerowInitializer {
        sdkSession = SdkSession(sdkId, sdkKey)
        return this
    }

    fun configPlatform(platform: HerowPlatform): HerowInitializer {
        this.platform = platform
        return this
    }

    fun configCustomId(customId: String): HerowInitializer {
        this.customId = customId
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
            .setInputData(
                workDataOf(
                    RequestWorker.KEY_SDK_ID to sdkSession.sdkId,
                    RequestWorker.KEY_SDK_KEY to sdkSession.sdkKey,
                    RequestWorker.KEY_CUSTOM_ID to customId,
                    RequestWorker.KEY_PLATFORM to platform.name
                )
            )
            .build()
        workerManager.enqueue(workerRequest)
    }
}