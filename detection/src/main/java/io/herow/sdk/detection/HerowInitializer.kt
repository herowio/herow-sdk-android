package io.herow.sdk.detection

import android.content.Context
import android.location.Location
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.jakewharton.threetenabp.AndroidThreeTen
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.connection.token.SdkSession
import io.herow.sdk.detection.analytics.LogsDispatcher
import io.herow.sdk.detection.analytics.LogsManager
import io.herow.sdk.detection.clickandcollect.ClickAndCollectDispatcher
import io.herow.sdk.detection.clickandcollect.ClickAndCollectListener
import io.herow.sdk.detection.clickandcollect.ClickAndCollectWorker
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.geofencing.GeofenceListener
import io.herow.sdk.detection.helpers.GeoHashHelper
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.location.LocationDispatcher
import io.herow.sdk.detection.location.LocationManager
import io.herow.sdk.detection.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class HerowInitializer private constructor(val context: Context) {
    private val appStateDetector = AppStateDetector()
    private var platform: HerowPlatform = HerowPlatform.PROD
    private var sdkSession = SdkSession("", "")
    private var customID: String = ""
    private var locationManager: LocationManager
    private var logsManager: LogsManager
    private var workManager: WorkManager
    private var database: HerowDatabase

    private lateinit var sessionHolder: SessionHolder
    private val initialRepeatInterval: Long = 900000

    init {
        AndroidThreeTen.init(context)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        workManager = WorkManager.getInstance(context)
        locationManager = LocationManager(context)
        logsManager = LogsManager(context)
        registerListeners()
        loadIdentifiers(context)
        database = HerowDatabase.getDatabase(context)
    }

    companion object {
        private lateinit var herowInitializer: HerowInitializer

        fun getInstance(context: Context): HerowInitializer {
            if (!::herowInitializer.isInitialized) {
                herowInitializer = HerowInitializer(context)
            }

            return herowInitializer
        }
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
        sessionHolder = SessionHolder(DataHolder(context))

        GlobalScope.launch(Dispatchers.IO) {
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

                GlobalLogger.shared.error(context,"Exception catched: $e - ${e.message}")
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

    fun setCustomId(customId: String): HerowInitializer {
        sessionHolder.saveCustomID(customId)
        this.customID = sessionHolder.getCustomID()
        return this
    }

    fun getCustomId(): String = sessionHolder.getCustomID()

    fun getOptinValue(): Boolean = sessionHolder.getOptinValue()

    fun acceptOptin(): HerowInitializer {
        saveOptinValue(true)
        return this
    }

    fun refuseOptin(): HerowInitializer {
        saveOptinValue(false)
        return this
    }

    fun synchronize() {
        if (sdkSession.hasBeenFilled()) {
            launchConfigRequest()
        } else {
            GlobalLogger.shared.error(context,"Credentials needed")
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
    fun launchConfigRequest() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatInterval: Long = if (sessionHolder.hasNoRepeatIntervalSaved()) {
            initialRepeatInterval
        } else {
            sessionHolder.getRepeatInterval()
        }

        GlobalLogger.shared.info(context,"LaunchConfigRequest method is called")

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(
                ConfigWorker::class.java,
                repeatInterval,
                TimeUnit.MILLISECONDS
            )
                .addTag(NetworkWorkerTags.CONFIG)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
                        AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
                        AuthRequests.KEY_CUSTOM_ID to customID,
                        AuthRequests.KEY_PLATFORM to platform.name
                    )
                )
                .build()
        workManager.enqueue(periodicWorkRequest)
        GlobalLogger.shared.info(context,"Config request is enqueued")
    }

    /**
     * Launch the cache request to get the zones the SDK must monitored
     */
    fun launchCacheRequest(location: Location) {
        GlobalLogger.shared.info(context,"LaunchCacheRequest method is called")

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
                        AuthRequests.KEY_CUSTOM_ID to customID,
                        AuthRequests.KEY_PLATFORM to platform.name,
                        CacheWorker.KEY_GEOHASH to GeoHashHelper.encodeBase32(location)
                    )
                )
                .build()
            workManager.enqueue(workerRequest)
        }
        GlobalLogger.shared.info(context,"Cache request is enqueued")
    }

    /**
     * Launch the logs request to send the events to he Herow Platform
     */
    fun launchLogsRequest(log: String) {
        val uuid = UUID.randomUUID().toString()
        LogsWorker.logsWorkerHashMap[uuid] = log
        GlobalLogger.shared.info(context,"CurrentID is $uuid")
        GlobalLogger.shared.info(context,"LaunchLogsRequest method is called")

        if (WorkHelper.isWorkNotScheduled(workManager, NetworkWorkerTags.CACHE)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            GlobalLogger.shared.info(context,"Log to send is: $log")

            val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<LogsWorker>()
                .addTag(NetworkWorkerTags.LOGS)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
                        AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
                        AuthRequests.KEY_CUSTOM_ID to customID,
                        AuthRequests.KEY_PLATFORM to platform.name,
                        LogsWorker.workerID to uuid
                    )
                )
                .build()

            workManager.enqueue(workerRequest)
            GlobalLogger.shared.info(context,"Log request is enqueued")
        }
    }

    fun launchClickAndCollect() {
        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(ClickAndCollectWorker::class.java)
            .addTag(ClickAndCollectWorker.tag)
            .build()
        workManager.enqueue(workRequest)
        sessionHolder.saveClickAndCollectProgress(true)
    }

    fun stopClickAndCollect() {
        workManager.cancelAllWorkByTag(ClickAndCollectWorker.tag)
        sessionHolder.saveClickAndCollectProgress(false)
    }

    fun isOnClickAndCollect(): Boolean = sessionHolder.getClickAndCollectProgress()

    fun registerClickAndCollectListener(listener: ClickAndCollectListener) {
        ClickAndCollectDispatcher.registerClickAndCollectListener(listener)
    }

    fun unregisterClickAndCollectListener(listener: ClickAndCollectListener) {
        ClickAndCollectDispatcher.unregisterClickAndCollectListener(listener)
    }

    fun registerEventListener(geofenceListener: GeofenceListener) {
        GlobalLogger.shared.info(context,"Register event listener called")
        GeofenceDispatcher.addGeofenceListener(geofenceListener)
    }

    fun registerCacheListener(cacheListener: CacheListener) {
        GlobalLogger.shared.info(context,"Register cache listener called")
        CacheDispatcher.addCacheListener(cacheListener)
    }

    fun fetchZonesInDatabase(): List<Zone>? {
        val zoneRepository = ZoneRepository(database.zoneDAO())
        return zoneRepository.getAllZones()
    }


    /**
     * Save user choice optin value
     */
    private fun saveOptinValue(optinAccepted: Boolean?) {
        sessionHolder.saveOptinValue(optinAccepted)
    }
}