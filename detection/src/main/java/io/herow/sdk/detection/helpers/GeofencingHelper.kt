package io.herow.sdk.detection.helpers

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import io.herow.sdk.connection.cache.model.Zone

object GeofencingHelper {
    fun buildGeofenceList(zones: List<Zone>): List<Geofence> {
        val geofenceList = ArrayList<Geofence>()
        for (zone: Zone in zones) {
            geofenceList.add(Geofence.Builder()
                .setRequestId(zone.hash)
                .setCircularRegion(zone.lat, zone.lng, zone.radius.toFloat())
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
        }
        return geofenceList
    }

    fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }
}