package io.herow.sdk.detection

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.ICacheListener
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.connection.token.SdkSession
import io.herow.sdk.detection.analytics.LogsDispatcher
import io.herow.sdk.detection.analytics.LogsManager
import io.herow.sdk.detection.cache.CacheManager
import io.herow.sdk.detection.clickandcollect.ClickAndCollectDispatcher
import io.herow.sdk.detection.clickandcollect.ClickAndCollectWorker
import io.herow.sdk.detection.clickandcollect.IClickAndCollectListener
import io.herow.sdk.detection.geofencing.GeofenceDispatcher
import io.herow.sdk.detection.geofencing.IGeofenceListener
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.location.ILocationListener
import io.herow.sdk.detection.location.LocationDispatcher
import io.herow.sdk.detection.location.LocationManager
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.ConfigWorker
import io.herow.sdk.detection.network.NetworkWorkerTags
import io.herow.sdk.detection.notification.NotificationManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject

@Suppress("MemberVisibilityCanBePrivate")
@SuppressLint("StaticFieldLeak")
class HerowInitializer private constructor(val context: Context) : ILocationListener, ICustomKoinComponent {
    private val appStateDetector = AppStateDetector()
    private var platform: HerowPlatform = HerowPlatform.PROD
    private var sdkSession = SdkSession("", "")
    private var customID: String = ""
    private var locationManager: LocationManager
    private var logsManager: LogsManager
    private var workManager: WorkManager
    private var notificationManager: NotificationManager
    private var cacheManager: CacheManager

    private var sessionHolder: SessionHolder

    //Koin
    private val herowDatabase: HerowDatabase by inject()
    private val ioDispatcher: CoroutineDispatcher by inject()
    private val zoneRepository: ZoneRepository by inject()


    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        sessionHolder = SessionHolder(DataHolder(context))
        workManager = WorkManager.getInstance(context)
        logsManager = LogsManager(context)
        cacheManager = CacheManager(context)
        loadIdentifiers(context)
        locationManager = LocationManager(context, sessionHolder, true)
        notificationManager = NotificationManager(context, sessionHolder)
        registerListeners()
    }

    companion object {
        private  var _testing = false
        @JvmStatic
        fun isTesting() : Boolean {
            return _testing
        }
        @JvmStatic
        fun setStaticTesting(staticTesting: Boolean)  {
            _testing = staticTesting
        }

        private lateinit var herowInitializer: HerowInitializer

        @JvmStatic
        fun getInstance(context: Context, testingStatus: Boolean = false): HerowInitializer {
            println("Value of testingStatus: $testingStatus")
            setStaticTesting(testingStatus)
            if (!::herowInitializer.isInitialized) {
                herowInitializer = HerowInitializer(context)
            }
            return herowInitializer
        }
    }

    private fun registerListeners() {
        AppStateDetector.addAppStateListener(locationManager)
        LocationDispatcher.addLocationListener(cacheManager)
        LocationDispatcher.addLocationListener(this)
        ConfigDispatcher.addConfigListener(locationManager)
        LogsDispatcher.addLogListener(logsManager)
        GeofenceDispatcher.addGeofenceListener(notificationManager)
    }

    /**
     * Try to load and save identifiers :
     * - DeviceId
     * - AdvertisingId: we use the admob library (https://developers.google.com/admob/android/quick-start),
     * but, we don't include it, we use the compileOnly flag (https://blog.gradle.org/introducing-compile-only-dependencies),
     * to be able to use it only if the developer has already the library include in his project.
     */
    private fun loadIdentifiers(context: Context) {
        CoroutineScope(ioDispatcher).launch {
            kotlin.runCatching {
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
                    GlobalLogger.shared.error(context, "Exception catched: $e - ${e.message}")
                    println("Your application does not implement the play-services-ads library")
                }
            }
        }
    }

    fun configApp(sdkId: String, sdkKey: String): HerowInitializer {
        sdkSession = SdkSession(sdkId, sdkKey)
        sessionHolder.saveSDKID(sdkId)
        sessionHolder.saveSdkKey(sdkKey)
        return this
    }

    fun configPlatform(platform: HerowPlatform): HerowInitializer {
        this.platform = platform
        sessionHolder.savePlatform(platform)
        return this
    }

    fun setCustomId(customId: String): HerowInitializer {
        sessionHolder.saveCustomID(customId)
        this.customID = sessionHolder.getCustomID()
        return this
    }

    fun getCustomId(): String = sessionHolder.getCustomID()

    fun getOptinValue(): Boolean = sessionHolder.getOptinValue()

    fun removeCustomId() {
        sessionHolder.removeCustomID()
    }

    fun acceptOptin(): HerowInitializer {
        saveOptinValue(true)
        return this
    }

    fun refuseOptin(): HerowInitializer {
        saveOptinValue(false)
        return this
    }

    fun synchronize() {
        Log.i("XXX", "Has been filled: ${sdkSession.hasBeenFilled()}")
        GlobalLogger.shared.info(context, "Hey there")
        if (sdkSession.hasBeenFilled()) {
            launchConfigRequest()
        } else {
            GlobalLogger.shared.error(context, "Credentials needed")
            println(
                "You need to enter your credentials before being able to use the SDK, with the " +
                        "configApp & configPlatform methods"
            )
        }
    }

    private fun shouldLaunchConfigRequest(): Boolean {
        if (sessionHolder.firstTimeLaunchingConfig()) {
            GlobalLogger.shared.debug(context, "Launch Config du to first launch")
            return true
        }

        val nextTimeToLaunch =
            sessionHolder.getLastConfigLaunch() + sessionHolder.getRepeatInterval()
        val currentTime = TimeHelper.getCurrentTime()

        if (currentTime > nextTimeToLaunch) {
            GlobalLogger.shared.debug(
                context,
                "Launch Config du config repeatInterval is done: currentTime = $currentTime, timeToRelaunch = $nextTimeToLaunch"
            )
            return true
        }

        return false
    }

    /**
     * Launch the necessary requests to configure the SDK & thus launch the geofencing monitoring.
     * Interval is by default 15 minutes
     */
    private fun launchConfigRequest() {
        if (shouldLaunchConfigRequest() && WorkHelper.isWorkNotScheduled(
                workManager,
                NetworkWorkerTags.CONFIG
            )
        ) {
            GlobalLogger.shared.debug(context, "launch ConfigRequest")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val datas = workDataOf(
                AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
                AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
                AuthRequests.KEY_CUSTOM_ID to customID,
                AuthRequests.KEY_PLATFORM to platform.name
            )
            val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<ConfigWorker>()
                .addTag(NetworkWorkerTags.CONFIG)
                .setConstraints(constraints)
                .setInputData(
                    datas
                )
                .build()
            workManager.enqueue(workerRequest)
            GlobalLogger.shared.info(context, "Config request is enqueued")
        } else {
            val lastConfig = sessionHolder.getConfig()
            if (lastConfig != null) {
                ConfigDispatcher.dispatchConfigResult(lastConfig)
            }
        }
    }

    fun didAcceptLocationUpdates() {
        launchConfigRequest()
    }

    fun launchClickAndCollect() {
        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(ClickAndCollectWorker::class.java)
            .addTag(ClickAndCollectWorker.tag)
            .build()
        workManager.enqueue(workRequest)
    }

    fun stopClickAndCollect() {
        workManager.cancelAllWorkByTag(ClickAndCollectWorker.tag)
        sessionHolder.saveClickAndCollectProgress(false)
    }

    fun isOnClickAndCollect(): Boolean = sessionHolder.getClickAndCollectProgress()

    fun registerClickAndCollectListener(listener: IClickAndCollectListener) {
        ClickAndCollectDispatcher.registerClickAndCollectListener(listener)
    }

    fun unregisterClickAndCollectListener(listener: IClickAndCollectListener) {
        ClickAndCollectDispatcher.unregisterClickAndCollectListener(listener)
    }

    fun registerEventListener(geofenceListener: IGeofenceListener) {
        GlobalLogger.shared.info(context, "Register event listener called")
        GeofenceDispatcher.addGeofenceListener(geofenceListener)
    }

    fun registerCacheListener(cacheListener: ICacheListener) {
        GlobalLogger.shared.info(context, "Register cache listener called")
        CacheDispatcher.addCacheListener(cacheListener)
    }

    fun fetchZonesInDatabase(): List<Zone>? = zoneRepository.getAllZones()

    /**
     * Save user choice optin value
     */
    private fun saveOptinValue(optinAccepted: Boolean?) {
        sessionHolder.saveOptinValue(optinAccepted)
        val datas = workDataOf(
            AuthRequests.KEY_SDK_ID to sdkSession.sdkId,
            AuthRequests.KEY_SDK_KEY to sdkSession.sdkKey,
            AuthRequests.KEY_CUSTOM_ID to customID,
            AuthRequests.KEY_PLATFORM to platform.name
        )

        CoroutineScope(ioDispatcher).launch {
            AuthRequests(sessionHolder, datas).getUserInfoIfNeeded()
        }
    }

    override fun onLocationUpdate(location: Location) {
        launchConfigRequest()
    }

    fun notificationsOnExactZoneEntry(value: Boolean) {
        notificationManager.notificationsOnExactZoneEntry(value)
    }

    fun reset(sdkId: String, sdkKey: String, customID: String) {
        sessionHolder.reset()

        CoroutineScope(ioDispatcher).launch {
            herowDatabase.clearAllTables()
            configureAfterReset(sdkId, sdkKey, customID)
        }
    }

    private fun configureAfterReset(sdkId: String, sdkKey: String, customID: String) {
        this.loadIdentifiers(context)
        this.configPlatform(HerowPlatform.PRE_PROD)
        this.configApp(sdkId, sdkKey)
        this.setCustomId(customID)
        this.acceptOptin()
        this.synchronize()
    }
}