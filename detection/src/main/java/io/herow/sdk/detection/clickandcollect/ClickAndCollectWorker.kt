
package io.herow.sdk.detection.clickandcollect

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.location.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.koin.core.component.inject

@Keep
@SuppressLint("InlinedApi")
class ClickAndCollectWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), ILocationPriorityListener, ICustomKoinComponent {
    private val sessionHolder: SessionHolder by inject()

    companion object {
        private const val LOCATION_REQUEST_CODE = 2021
        const val tag = "detection_ForegroundLocationWorker"
    }

    init {
        LocationPriorityDispatcher.registerLocationPriorityListener(this)
    }

    private val pendingIntent = createPendingIntent(applicationContext)

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            LOCATION_REQUEST_CODE,
            intent,
            pendingIntent
        )
    }

    override suspend fun doWork(): Result {
        return coroutineScope {
            val job = async {
                ClickAndCollectDispatcher.didStartClickAndCollect()
                launchJob()
                return@async Result.success()
            }

            job.invokeOnCompletion { exception: Throwable? ->
                if (exception != null) {
                    GlobalLogger.shared.error(null, "Click and Collect stops due to $exception")
                } else {
                    GlobalLogger.shared.error(null, "Click and Collect normaly stops")
                }
                sessionHolder.saveClickAndCollectProgress(false)
                ClickAndCollectDispatcher.didStopClickAndCollect()
            }
            job.await()
        }
    }

    private suspend fun launchJob() {
        NotificationHelper.createNotificationChannel(applicationContext)
        val foregroundInfo = NotificationHelper.foregroundNotification(applicationContext, id)
        setForeground(foregroundInfo)

        GlobalLogger.shared.info(applicationContext, "ClickAndCollect - Into launchJob")

        if (hasLocationPermission()) {
            launchLocationsUpdate()
        }
        // In order to test only
        if (TimeHelper.testing) {
            delay(TimeHelper.TWO_SECONDS_MS)
        } else {
            delay(TimeHelper.TWO_HOUR_MS)
        }

        if (hasLocationPermission()) {
            stopLocationsUpdate()
        }
    }


    private fun hasLocationPermission(): Boolean {
        val accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        val backLocation = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            accessFineLocation
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            applicationContext,
            coarseLocation
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            applicationContext,
            backLocation
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildLocationRequest(locationPriority: LocationPriority): LocationRequest {
        return LocationRequest.create().apply {
            smallestDisplacement = locationPriority.smallestDistance.toFloat()
            fastestInterval = TimeHelper.TEN_SECONDS_MS
            interval = locationPriority.interval
            priority = locationPriority.priority
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateMonitoring(priority: LocationPriority) {
        GlobalLogger.shared.debug(null, "update priority  : $priority")
        val locationRequest = buildLocationRequest(priority)
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationProviderClient.removeLocationUpdates(pendingIntent).addOnCompleteListener {
            GlobalLogger.shared.debug(null, "relaunch with priority : $priority")
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, pendingIntent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun launchLocationsUpdate() {
        val locationRequest = buildLocationRequest()
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, pendingIntent)
    }

    private fun buildLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            fastestInterval = TimeHelper.TEN_SECONDS_MS
            interval = LocationPriority.HIGH.interval
            priority = LocationPriority.HIGH.priority
            smallestDisplacement = LocationPriority.HIGH.smallestDistance.toFloat()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopLocationsUpdate() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationProviderClient.removeLocationUpdates(pendingIntent)
    }

    override fun onLocationPriority(priority: LocationPriority) {
        updateMonitoring(priority)
    }
}