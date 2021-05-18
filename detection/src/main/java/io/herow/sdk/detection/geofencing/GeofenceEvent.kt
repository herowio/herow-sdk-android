package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.connection.cache.model.Zone

data class GeofenceEvent(
    val zone: Zone,
    val location: Location,
    val type: GeofenceType
    //val confiance: Int
)