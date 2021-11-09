package io.herow.sdk.detection

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import io.herow.sdk.common.helpers.Constants
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
import kotlinx.coroutines.SupervisorJob
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
    private val logsManager: LogsManager = LogsManager(context)
    private val workManager: WorkManager = WorkManager.getInstance(context)
    private var notificationManager: NotificationManager
    private val cacheManager: CacheManager = CacheManager(context)

    private lateinit var preprodURL: String
    private lateinit var prodURL: String

    //Koin
    private val sessionHolder: SessionHolder by inject()
    private val herowDatabase: HerowDatabase by inject()
    private val ioDispatcher: CoroutineDispatcher by inject()
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val zoneRepository: ZoneRepository by inject()


    init {
        loadIdentifiers(context)
        locationManager = LocationManager(context, true)
        notificationManager = NotificationManager(context)
        registerListeners()
    }

    companion object {
        private var testing = false

        @JvmStatic
        fun isTesting(): Boolean {
            return testing
        }

        @JvmStatic
        fun setStaticTesting(staticTesting: Boolean) {
            testing = staticTesting
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
        applicationScope.launch {
            ProcessLifecycleOwner.get().lifecycle.addObserver(appStateDetector)
        }

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
        applicationScope.launch {
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

    fun configApp(sdkKey: String, sdkSecret: String): HerowInitializer {
        sdkSession = SdkSession(sdkKey, sdkSecret)
        sessionHolder.saveSDKID(sdkKey)
        sessionHolder.saveSdkKey(sdkSecret)
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

    fun removeCustomId() = sessionHolder.removeCustomID()

    fun setPreProdCustomURL(url: String) = resetForCustomURL(true, url)

    fun setProdCustomURL(url: String) = resetForCustomURL(false, url)

    fun removeCustomURL() {
        sessionHolder.removeCustomURL()

        if (sessionHolder.getPlatformName() == HerowPlatform.PRE_PROD) {
            resetForCustomURL(true, "")
        } else {
            resetForCustomURL(false, "")
        }
    }

    fun getCurrentURL(): String = sessionHolder.getCurrentURL()

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
        sessionHolder.saveClickAndCollectProgress(true)
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

        applicationScope.launch {
            AuthRequests(datas).getUserInfoIfNeeded()
        }
    }

    override fun onLocationUpdate(location: Location) {
        launchConfigRequest()
    }

    fun notificationsOnExactZoneEntry(value: Boolean) {
        notificationManager.notificationsOnExactZoneEntry(value)
    }

    private fun resetForCustomURL(isPreprod: Boolean, newURL: String) {
        val identifiers: Map<String, String> =
            mapOf(
                Pair(if (isPreprod) Constants.CUSTOM_PRE_PROD_URL else Constants.CUSTOM_PROD_URL, newURL)
            )

        configureAfterCustomURLChange(
            identifiers,
            isPreprod
        )
    }

    private fun configureAfterCustomURLChange(
        identifiers: Map<String, String>,
        preprod: Boolean
    ) {
        sessionHolder.resetForCustomURL()
        if (preprod) sessionHolder.saveCustomPreProdURL(identifiers[Constants.CUSTOM_PRE_PROD_URL]!!) else sessionHolder.saveCustomProdURL(
            identifiers[Constants.CUSTOM_PROD_URL]!!
        )

        clearAllTablesForCustomURL(preprod)
    }

    fun resetForAccount(sdkId: String, sdkKey: String, customID: String) {
        reset()
        clearAllTables(sdkId, sdkKey, customID)
    }

    private fun reset() = sessionHolder.reset()

    private fun clearAllTablesForCustomURL(isPreprod: Boolean) {
        applicationScope.launch {
            herowDatabase.clearAllTables()
            configureAfterResetForCustomURL(isPreprod)
        }
    }

    private fun clearAllTables(sdkId: String, sdkKey: String, customID: String) {
        applicationScope.launch {
            herowDatabase.clearAllTables()
            configureAfterResetForAccount(sdkId, sdkKey, customID)
        }
    }

    private fun configureAfterResetForCustomURL(isPreprod: Boolean) {
        val sdkID = sessionHolder.getSDKID()
        val sdkKey = sessionHolder.getSdkKey()
        val customID = sessionHolder.getCustomID()

        this.loadIdentifiers(context)
        this.configPlatform(if (isPreprod) HerowPlatform.PRE_PROD else HerowPlatform.PROD)
        this.configApp(sdkID, sdkKey)
        this.setCustomId(customID)
        this.acceptOptin()
        this.synchronize()
    }

    private fun configureAfterResetForAccount(sdkId: String, sdkKey: String, customID: String) {
        this.loadIdentifiers(context)
        this.configPlatform(HerowPlatform.PRE_PROD)
        this.configApp(sdkId, sdkKey)
        this.setCustomId(customID)
        this.acceptOptin()
        this.synchronize()
    }

    fun getSDKVersion(): String = BuildConfig.SDK_VERSION
}