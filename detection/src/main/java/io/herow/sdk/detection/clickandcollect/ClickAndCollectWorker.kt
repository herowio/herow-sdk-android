package io.herow.sdk.detection.clickandcollect

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.location.LocationReceiver
import io.herow.sdk.detection.location.NotificationHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class ClickAndCollectWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    companion object {
        private const val LOCATION_REQUEST_CODE = 2021
        const val tag = "detection_ForegroundLocationWorker"
    }

    private val pendingIntent = createPendingIntent(applicationContext)

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            LOCATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))

        return coroutineScope {
            val job = async {
                ClickAndCollectDispatcher.didStartClickAndCollect()
                launchJob()
                return@async Result.success()
            }
            job.invokeOnCompletion { exception: Throwable? ->
                when (exception) {
                    is CancellationException -> {
                        sessionHolder.saveClickAndCollectProgress(false)
                        ClickAndCollectDispatcher.didStopClickAndCollect()
                    }
                    else -> {
                    }
                }
            }
            job.await()
        }
    }

    private suspend fun launchJob() {
        NotificationHelper.createNotificationChannel(applicationContext)
        val foregroundInfo = NotificationHelper.foregroundNotification(applicationContext, id)
        setForeground(foregroundInfo)

        if (hasLocationPermission()) {
            launchLocationsUpdate()
        }
        delay(TimeHelper.TWO_HOUR_MS)
        if (hasLocationPermission()) {
            stopLocationsUpdate()
        }
    }

    private fun hasLocationPermission(): Boolean {
        val accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            accessFineLocation
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            applicationContext,
            coarseLocation
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun launchLocationsUpdate() {
        val locationRequest = buildLocationRequest()
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, pendingIntent)
    }

    private fun buildLocationRequest(): LocationRequest {
        val request = LocationRequest()
        request.fastestInterval = TimeHelper.THREE_MINUTE_MS
        request.interval = TimeHelper.ONE_MINUTE_MS
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return request
    }

    @SuppressLint("MissingPermission")
    private fun stopLocationsUpdate() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        fusedLocationProviderClient.removeLocationUpdates(pendingIntent)
    }
}