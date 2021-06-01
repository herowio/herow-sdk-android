package io.herow.sdk.detection.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import io.herow.sdk.common.logger.GlobalLogger

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            GlobalLogger.shared.info(context, "LocationResult.hasResult: $intent")
            val locationResult = LocationResult.extractResult(intent)

            GlobalLogger.shared.info(context, "LocationResult is: $locationResult")
            LocationDispatcher.dispatchLocation(locationResult.lastLocation)
            GlobalLogger.shared.info(context, "Dispatching location is done")
        }
    }
}