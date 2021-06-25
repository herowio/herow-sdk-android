package io.herow.sdk.detection.cache

import android.content.Context
import android.location.Location
import androidx.work.*
import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.geofencing.model.LocationMapper
import io.herow.sdk.detection.geofencing.model.toLocationMapper
import io.herow.sdk.detection.helpers.DateHelper
import io.herow.sdk.detection.helpers.GeoHashHelper
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.location.LocationListener
import io.herow.sdk.detection.location.LocationManager
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.CacheWorker
import io.herow.sdk.detection.network.NetworkWorkerTags
import kotlinx.coroutines.launch

class CacheManager(val context: Context): LocationListener {

    private val workManager = WorkManager.getInstance(context)
    private val sessionHolder = SessionHolder(DataHolder(context))
    private val workOfData = WorkHelper.getWorkOfData(sessionHolder)
    private val platform = WorkHelper.getPlatform(sessionHolder)

    /**
     * Launch cache request if
     * - Geohash is different from the old one
     * -
     */
    override fun onLocationUpdate(location: Location) {
        GlobalLogger.shared.info(context, "Into onLocationUpdate from CacheManager")
        if (shouldGetCache(sessionHolder, location)) {
            LocationManager.scope.launch { CacheManager(context).launchCacheRequest(location) }
        }
    }

    /**
     * Launch the cache request to get the zones the SDK must monitored
     */
    private fun launchCacheRequest(location: Location) {
        GlobalLogger.shared.info(context, "LaunchCacheRequest method is called")

        if (WorkHelper.isWorkNotScheduled(workManager, NetworkWorkerTags.CACHE)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val locationMapper = LocationMapper().toLocationMapper(location)

            val workerRequest: WorkRequest = OneTimeWorkRequestBuilder<CacheWorker>()
                .addTag(NetworkWorkerTags.CACHE)
                .setConstraints(constraints)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to workOfData[Constants.SDK_ID],
                        AuthRequests.KEY_SDK_KEY to workOfData[Constants.SDK_KEY],
                        AuthRequests.KEY_CUSTOM_ID to workOfData[Constants.CUSTOM_ID],
                        AuthRequests.KEY_PLATFORM to platform[Constants.PLATFORM]!!.name,
                        CacheWorker.KEY_GEOHASH to sessionHolder.getGeohash(),
                        Constants.LOCATION_DATA to Gson().toJson(locationMapper)
                    )
                )
                .build()

            workManager.enqueue(workerRequest)
            GlobalLogger.shared.info(context, "Cache request is enqueued")
        }
    }

    /**
     * First check if GeoHash has already been saved into SP
     * Then check if GeoHash is empty or different from saved data
     */
    private fun isGeoHashUnknownOrDifferent(sessionHolder: SessionHolder, location: Location): Boolean {
        val inputGeohash: String = GeoHashHelper.encodeBase32(location).substring(0,4)
        val noGeoHash =  sessionHolder.hasNoGeoHashSaved()
        val oldGeoHash = sessionHolder.getGeohash()
        val differentGeoHash =  inputGeohash != sessionHolder.getGeohash()
        GlobalLogger.shared.debug(context, "geohash state: noGeoHash = $noGeoHash, differentGeoHash=$differentGeoHash ")
        GlobalLogger.shared.debug(context, "geohash state: savedGeoHash = $oldGeoHash, newGeo=$inputGeohash ")

        if (noGeoHash || differentGeoHash) {
            sessionHolder.saveGeohash(inputGeohash)
            return true
        }

        return false
    }

    private fun shouldFetchNow(sessionHolder: SessionHolder): Boolean {
        val expirationCacheTime = DateHelper.convertStringToTimeStampInMilliSeconds(sessionHolder.getLastSavedModifiedDateTimeCache())
        val lastTimeCacheWasLaunched = sessionHolder.getLastTimeCacheWasLaunched()

        return lastTimeCacheWasLaunched == 0L || lastTimeCacheWasLaunched < expirationCacheTime
    }

    private fun shouldGetCache(sessionHolder: SessionHolder, location: Location): Boolean {
        val differentHash = isGeoHashUnknownOrDifferent(sessionHolder, location)
        val lastFetchDate = sessionHolder.getLastTimeCacheWasLaunched()
        val lastCacheModifiedDate = DateHelper.convertStringToTimeStampInMilliSeconds(sessionHolder.getLastSavedModifiedDateTimeCache())

        GlobalLogger.shared.info(context, "Cache already launched: ${sessionHolder.didSaveLastLaunchCache()}")

        if (!sessionHolder.didSaveLastLaunchCache()) {
            return true
        }

        val cacheIsNotUpToDate = lastFetchDate < lastCacheModifiedDate
        val fetchNow = shouldFetchNow(sessionHolder)

        GlobalLogger.shared.info(context, "Last fetch date: $lastFetchDate")
        GlobalLogger.shared.info(context, "Last cache modified: $lastCacheModifiedDate")

        if (differentHash) {
            GlobalLogger.shared.debug(context, "Cache should be fetch because of different hash")
        }

        if (cacheIsNotUpToDate) {
            GlobalLogger.shared.debug(context, "Cache should be fetch because of cache is not up to date")
        }

        if (fetchNow) {
            GlobalLogger.shared.debug(context, "Cache should be fetch because of cache interval is done")
        }

        return differentHash || cacheIsNotUpToDate || fetchNow
    }
}