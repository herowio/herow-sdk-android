package io.herow.sdk.detection

import android.content.Context
import android.location.Location
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.jakewharton.threetenabp.AndroidThreeTen
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.common.states.motion.ActivityTransitionDetector
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.token.SdkSession
import io.herow.sdk.detection.analytics.LogsDispatcher
import io.herow.sdk.detection.analytics.LogsManager
import io.herow.sdk.detection.helpers.GeoHashHelper
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.location.ClickAndCollectWorker
import io.herow.sdk.detection.location.LocationDispatcher
import io.herow.sdk.detection.location.LocationManager
import io.herow.sdk.detection.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object HerowInitializer {
    private val activityTransitionDetector = ActivityTransitionDetector()
    private val appStateDetector = AppStateDetector()
    private var customId: String = ""
    private var platform: HerowPlatform = HerowPlatform.PROD
    private var sdkSession = SdkSession("", "")
    private lateinit var locationManager: LocationManager
    private lateinit var logsManager: LogsManager
    private lateinit var workManager: WorkManager

    private lateinit var sessionHolder: SessionHolder
    private val initialRepeatInterval: Long = 900000

    fun init(context: Context): HerowInitializer {
        AndroidThreeTen.init(context)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        activityTransitionDetector.launchTransitionMonitoring(context)
        workManager = WorkManager.getInstance(context)
        locationManager = LocationManager(context)
        logsManager = LogsManager(context)
        registerListeners()
        loadIdentifiers(context)
        return this
    }

    private fun registerListeners() {
        AppStateDetector.addAppStateListener(locationManager)
        LocationDispatcher.addLocationListener(locationManager)
        ConfigDispatcher.addConfigListener(locationManager)
        LogsDispatcher.addLogListener(logsManager)
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
            sessionHolder = SessionHolder(DataHolder(context))
            val deviceId = DeviceHelper.getDeviceId(context)
            sessionHolder.saveDeviceId(deviceId)
            try {
                val advertiserInfo: AdvertisingIdClient.Info =
                    AdvertisingIdClient.getAdvertisingIdInfo(
                        context
                    )
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
            launchConfigRequest()
        } else {
            println(
                "You need to enter your credentials before being able to use the SDK, with the " +
                        "configApp & configPlatform methods"
            )
        }
    }

    /**
     * Launch the necessary requests to configure the SDK & thus launch the geofencing monitoring.
     * Interval is by default 15 minutes
     */
    private fun launchConfigRequest() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        var repeatInterval: Long = if (sessionHolder.hasNoRepeatIntervalSaved()) {
            initialRepeatInterval
        } else {
            sessionHolder.getRepeatInterval()
        }

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(ConfigWorker::class.java, repeatInterval, TimeUnit.MILLISECONDS)
                .addTag(NetworkWorkerTags.CONFIG)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
                        AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
                        AuthRequests.KEY_CUSTOM_ID to customId,
                        AuthRequests.KEY_PLATFORM to platform.name
                    )
                )
                .build()
        workManager.enqueue(periodicWorkRequest)
    }

    /**
     * Launch the cache request to get the zones the SDK must monitored
     */
    fun launchCacheRequest(location: Location) {
        if (WorkHelper.isWorkNotScheduled(workManager, NetworkWorkerTags.CACHE)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<CacheWorker>()
                .addTag(NetworkWorkerTags.CACHE)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
                        AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
                        AuthRequests.KEY_CUSTOM_ID to customId,
                        AuthRequests.KEY_PLATFORM to platform.name,
                        CacheWorker.KEY_GEOHASH to GeoHashHelper.encodeBase32(location)
                    )
                )
                .build()
            workManager.enqueue(workerRequest)
        }
    }

    /**
     * Launch the logs request to send the events to he Herow Platform
     */
    fun launchLogsRequest(logs: String) {
        if (WorkHelper.isWorkNotScheduled(workManager, NetworkWorkerTags.CACHE)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<LogsWorker>()
                .addTag(NetworkWorkerTags.LOGS)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
                        AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
                        AuthRequests.KEY_CUSTOM_ID to customId,
                        AuthRequests.KEY_PLATFORM to platform.name,
                        LogsWorker.KEY_LOGS to logs
                    )
                )
                .build()
            workManager.enqueue(workerRequest)
        }
    }

    fun launchClickAndCollect() {
        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(ClickAndCollectWorker::class.java)
            .addTag(ClickAndCollectWorker.tag)
            .build()
        workManager.enqueue(workRequest)
    }

    fun stopClickAndCollect() {
        workManager.cancelAllWorkByTag(ClickAndCollectWorker.tag)
    }
}