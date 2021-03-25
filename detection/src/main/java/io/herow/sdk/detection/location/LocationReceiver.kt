package io.herow.sdk.detection.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            Log.i("XXX/EVENT", "LocationReceiver - LocationResult.hasResult: $intent")
            val locationResult = LocationResult.extractResult(intent)

            Log.i("XXX/EVENT", "LocationReceiver - LocationResult is: $locationResult")
            if (locationResult != null && locationResult.lastLocation != null) {
                LocationDispatcher.dispatchLocation(locationResult.lastLocation)
                Log.i("XXX/EVENT", "LocationReceiver - Dispatching location is done")
            }
        }
    }
}