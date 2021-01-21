package io.herow.sdk.detection

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.jakewharton.threetenabp.AndroidThreeTen
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.connection.SessionHolder
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
    private var sdkSession = SdkSession("", "")
    private lateinit var workerManager: WorkManager

    fun init(context: Context): HerowInitializer {
        AndroidThreeTen.init(context)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        activityTransitionDetector.launchTransitionMonitoring(context)
        workerManager = WorkManager.getInstance(context)
        loadIdentifiers(context)
        return this
    }

    /**
     * Try to load and save identifiers :
     * - DeviceId
     * - AdvertisingId: we use the admob library (https://developers.google.com/admob/android/quick-start),
     * but, we don't include it, we use the compileOnly flag (https://blog.gradle.org/introducing-compile-only-dependencies),
     * to be able to use it only if the developer has already the library include in his project.
     */
    private fun loadIdentifiers(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val sessionHolder = SessionHolder(DataHolder(context))
            val deviceId = DeviceHelper.getDeviceId(context)
            sessionHolder.saveDeviceId(deviceId)
            try {
                val advertiserInfo: AdvertisingIdClient.Info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                if (!advertiserInfo.isLimitAdTrackingEnabled) {
                    sessionHolder.saveAdvertiserId(advertiserInfo.id)
                }
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
        if (sdkSession.hasBeenFilled()) {
            launchRequests()
        } else {
            println("You need to enter your credentials before being able to use the SDK, with the " +
                    "configApp & configPlatform methods")
        }
    }

    /**
     * Launch the necessary requests to configure the SDK & thus launch the geofencing monitoring.
     */
    private fun launchRequests() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<ConfigWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    ConfigWorker.KEY_SDK_ID to sdkSession.sdkId,
                    ConfigWorker.KEY_SDK_KEY to sdkSession.sdkKey,
                    ConfigWorker.KEY_CUSTOM_ID to customId,
                    ConfigWorker.KEY_PLATFORM to platform.name
                )
            )
            .build()
        workerManager.enqueue(workerRequest)
    }
}