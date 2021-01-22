package io.herow.sdk.detection.location

import android.location.Location

interface LocationListener {
    fun onLocationUpdate(location: Location)
}