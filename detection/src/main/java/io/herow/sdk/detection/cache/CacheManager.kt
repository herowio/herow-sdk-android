package io.herow.sdk.detection.cache

import android.content.Context
import android.location.Location
import androidx.annotation.Keep
import androidx.work.*
import com.google.gson.Gson
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.geofencing.model.toLocationMapper
import io.herow.sdk.detection.helpers.DateHelper
import io.herow.sdk.detection.helpers.GeoHashHelper
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.location.ILocationListener
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.CacheWorker
import io.herow.sdk.detection.network.NetworkWorkerTags
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.inject

@Keep
class CacheManager(val context: Context) : ILocationListener, ICustomKoinComponent {

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val workManager = WorkManager.getInstance(context)
    private val sessionHolder: SessionHolder by inject()
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun onLocationUpdate(location: Location) {
        val shouldGetCache = shouldGetCache(location)

        GlobalLogger.shared.info(context, "Should get cache: $shouldGetCache")

        if (shouldGetCache) {
            applicationScope.launch {
                launchCacheRequest(location)
            }
        }
    }

    /**
     * Launch the cache request to get the zones the SDK must be monitoring
     */
    private fun launchCacheRequest(location: Location) {
        GlobalLogger.shared.info(context, "LaunchCacheRequest method is called")

        if (WorkHelper.isWorkNotScheduled(workManager, NetworkWorkerTags.CACHE)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val locationMapper = toLocationMapper(location)

            val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<CacheWorker>()
                .addTag(NetworkWorkerTags.CACHE)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to sessionHolder.getSDKID(),
                        AuthRequests.KEY_SDK_KEY to sessionHolder.getSdkKey(),
                        AuthRequests.KEY_CUSTOM_ID to sessionHolder.getCustomID(),
                        AuthRequests.KEY_PLATFORM to sessionHolder.getPlatformName().name,
                        CacheWorker.KEY_GEOHASH to sessionHolder.getGeohash(),
                        Constants.LOCATION_DATA to Gson().toJson(locationMapper)
                    )
                )
                .build()

            workManager.enqueue(workerRequest)
            GlobalLogger.shared.info(context, "Cache request is enqueued")
        } else {
            GlobalLogger.shared.info(context, "Cache request is not enqueued")
        }
    }

    /**
     * First check if GeoHash has already been saved into SP
     * Then check if GeoHash is empty or different from saved data
     */
    private fun isGeoHashUnknownOrDifferent(
        location: Location
    ): Boolean {
        val inputGeohash: String = GeoHashHelper.encodeBase32(location).substring(0, 4)
        val noGeoHash = sessionHolder.hasNoGeoHashSaved()
        val oldGeoHash = sessionHolder.getGeohash()
        val differentGeoHash = inputGeohash != sessionHolder.getGeohash()
        GlobalLogger.shared.debug(
            context,
            "geohash state: noGeoHash = $noGeoHash, differentGeoHash=$differentGeoHash "
        )
        GlobalLogger.shared.debug(
            context,
            "geohash state: savedGeoHash = $oldGeoHash, newGeo=$inputGeohash "
        )

        if (noGeoHash || differentGeoHash) {
            sessionHolder.saveGeohash(inputGeohash)
            return true
        }

        return false
    }

    private fun shouldFetchNow(): Boolean {
        val expirationCacheTime =
            DateHelper.convertStringToTimeStampInMilliSeconds(sessionHolder.getLastSavedModifiedDateTimeCache())
        val lastTimeCacheWasLaunched = sessionHolder.getLastTimeCacheWasLaunched()

        return lastTimeCacheWasLaunched == 0L || lastTimeCacheWasLaunched < expirationCacheTime
    }

    private fun shouldGetCache(location: Location): Boolean {
        val differentHash = isGeoHashUnknownOrDifferent(location)
        val lastFetchDate = sessionHolder.getLastTimeCacheWasLaunched()
        val lastCacheModifiedDate: Long =
            if (sessionHolder.getLastSavedModifiedDateTimeCache().isEmpty()) {
                0L
            } else {
                DateHelper.convertStringToTimeStampInMilliSeconds(sessionHolder.getLastSavedModifiedDateTimeCache())
            }

        GlobalLogger.shared.info(
            context,
            "Cache already launched: ${sessionHolder.didSaveLastLaunchCache()}"
        )

        if (!sessionHolder.didSaveLastLaunchCache()) {
            return true
        }

        val cacheIsNotUpToDate = lastFetchDate < lastCacheModifiedDate
        val fetchNow = shouldFetchNow()

        GlobalLogger.shared.info(context, "Last fetch date: $lastFetchDate")
        GlobalLogger.shared.info(context, "Last cache modified: $lastCacheModifiedDate")

        if (differentHash) {
            GlobalLogger.shared.debug(context, "Cache should be fetch because of different hash")
        }

        if (cacheIsNotUpToDate) {
            GlobalLogger.shared.debug(
                context,
                "Cache should be fetch because of cache is not up to date"
            )
        }

        if (fetchNow) {
            GlobalLogger.shared.debug(
                context,
                "Cache should be fetch because of cache interval is done"
            )
        }

        return differentHash || cacheIsNotUpToDate || fetchNow
    }
}