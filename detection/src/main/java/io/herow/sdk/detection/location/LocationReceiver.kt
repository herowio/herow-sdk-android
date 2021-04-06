package io.herow.sdk.detection.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult
import io.herow.sdk.common.logger.GlobalLogger

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            GlobalLogger.shared.info(
                context,
                "LocationReceiver",
                "onReceive",
                13,
                "LocationResult.hasResult: $intent"
            )
            val locationResult = LocationResult.extractResult(intent)

            GlobalLogger.shared.info(
                context,
                "LocationReceiver",
                "onReceive",
                16,
                "LocationResult is: $locationResult"
            )
            if (locationResult != null && locationResult.lastLocation != null) {
                LocationDispatcher.dispatchLocation(locationResult.lastLocation)
                GlobalLogger.shared.info(
                    context,
                    "LocationReceiver",
                    "onReceive",
                    19,
                    "Dispatching location is done"
                )
            }
        }
    }
}