package io.herow.sdk.detection.location

import com.google.android.gms.location.LocationRequest
import io.herow.sdk.common.helpers.TimeHelper

enum class LocationPriority(
    val interval: Long,
    val smallestDistance: Double,
    val priority: Int
) {
    VERY_LOW(TimeHelper.THREE_MINUTE_MS, 300.0, LocationRequest.PRIORITY_LOW_POWER),
    LOW(TimeHelper.ONE_MINUTE_MS, 100.0, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
    MEDIUM(TimeHelper.TWENTY_SECONDS_MS, 50.0, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
    HIGH(TimeHelper.FIVE_SECONDS_MS, 10.0, LocationRequest.PRIORITY_HIGH_ACCURACY),
    VERY_HIGH(TimeHelper.TWO_SECONDS_MS, 5.0, LocationRequest.PRIORITY_HIGH_ACCURACY)
}



