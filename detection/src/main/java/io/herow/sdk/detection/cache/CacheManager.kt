package io.herow.sdk.detection.cache

import android.content.Context
import android.location.Location
import androidx.work.*
import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.geofencing.model.toLocationMapper
import io.herow.sdk.detection.helpers.DateHelper
import io.herow.sdk.detection.helpers.GeoHashHelper
import io.herow.sdk.detection.helpers.WorkHelper
import io.herow.sdk.detection.network.AuthRequests
import io.herow.sdk.detection.network.CacheWorker
import io.herow.sdk.detection.network.NetworkWorkerTags

class CacheManager(val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val sessionHolder = SessionHolder(DataHolder(context))
    private val workOfData = WorkHelper.getWorkOfData(sessionHolder)
    private val platform = WorkHelper.getPlatform(sessionHolder)

    /**
     * Launch the cache request to get the zones the SDK must monitored
     */
    fun launchCacheRequest(location: Location) {
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
                        AuthRequests.KEY_SDK_ID to workOfData[Constants.SDK_ID],
                        AuthRequests.KEY_SDK_KEY to workOfData[Constants.SDK_KEY],
                        AuthRequests.KEY_CUSTOM_ID to workOfData[Constants.CUSTOM_ID],
                        AuthRequests.KEY_PLATFORM to platform[Constants.PLATFORM]!!.name,
                        CacheWorker.KEY_GEOHASH to GeoHashHelper.encodeBase32(location),
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
    fun isGeoHashUnknownOrDifferent(sessionHolder: SessionHolder, location: Location): Boolean {
        val inputGeohash: String = GeoHashHelper.encodeBase32(location)

        if (sessionHolder.hasNoGeoHashSaved() || inputGeohash != sessionHolder.getGeohash()) {
            return true
        }

        return false
    }

    fun shouldFetchNow(sessionHolder: SessionHolder): Boolean {
        val expirationCacheTime = DateHelper.convertStringToTimeStamp(sessionHolder.getLastSavedModifiedDateTimeCache())
        val lastTimeCacheWasLaunched = sessionHolder.getLastTimeCacheWasLaunched()

        return lastTimeCacheWasLaunched == 0L || lastTimeCacheWasLaunched > expirationCacheTime
    }
}