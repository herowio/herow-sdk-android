package io.herow.sdk.detection.location

import android.location.Location

interface ILocationListener {
    fun onLocationUpdate(location: Location)
}