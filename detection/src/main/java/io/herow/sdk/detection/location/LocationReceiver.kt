package io.herow.sdk.detection.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationResult

class LocationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            if (locationResult != null && locationResult.lastLocation != null) {
                LocationDispatcher.dispatchLocation(locationResult.lastLocation)
            }
        }
    }
}