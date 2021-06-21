package io.herow.sdk.detection.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.detection.location.LocationDispatcher

class GeofencingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)
        if (!geofencingEvent.hasError()) {
            val location = geofencingEvent.triggeringLocation
            if (location != null ){
            LocationDispatcher.dispatchLocation(location)
            }
        } else {
            val errorMessage: String =
                GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            GlobalLogger.shared.error(context, "Error message is: $errorMessage")

            println(errorMessage)
        }
    }
}