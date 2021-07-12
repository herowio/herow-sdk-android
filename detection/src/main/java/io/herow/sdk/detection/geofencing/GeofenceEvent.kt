package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.geofencing.model.LocationMapper
import kotlin.math.*

data class GeofenceEvent(
    val zone: Zone,
    val location: Location,
    val type: GeofenceType,
    var confidence: Double = 12.0
) {

    fun computeEnterConfidence(location: Location, zone: Zone): Double {
        zone.radius = zone.radius ?: 0.0
        val confidence = computeConfidence(location, zone)
        GlobalLogger.shared.debug(null, "ComputeEnter: $location & $zone")
        GlobalLogger.shared.debug(null, "GeofenceEvent enter zone confidence: $confidence")

        return confidence
    }

    fun computeNotificationConfidence(location: Location, zone: Zone): Double {
        zone.radius = 3.times(zone.radius ?: 0.0)
        val confidence = computeConfidence(location, zone)
        GlobalLogger.shared.debug(null, "ComputeNotification: $location & $zone")
        GlobalLogger.shared.debug(
            null,
            "GeofenceEvent enter notification zone confidence: $confidence"
        )

        return confidence
    }

    fun computeExitConfidence(location: Location, zone: Zone): Double {
        zone.radius = zone.radius ?: 0.0
        val confidence = 1 - computeConfidence(location, zone)
        GlobalLogger.shared.debug(null, "GeofenceEvent exit zone confidence: $confidence")

        return confidence
    }

    private fun computeConfidence(location: Location, zone: Zone): Double {
        GlobalLogger.shared.info(null, "GeofenceEvent - Parameters are: $location and $zone")

        if (location.accuracy.compareTo(0.0) == 0) {
            return 0.0
        }

        val center = Location("").apply {
            latitude = zone.lat!!
            longitude = zone.lng!!
        }

        val distance: Double = center.distanceTo(location).toDouble()
        val accuracyRadius = location.accuracy.toDouble()

        GlobalLogger.shared.info(
            null,
            "Value of distance is: $distance and value of accuracyRadius is: $accuracyRadius"
        )

        /*if (accuracyRadius.compareTo(0.0) == 0) {
            return 0.0
        }*/

        val radius1 = max(zone.radius!!, accuracyRadius)
        val radius2 = min(zone.radius!!, accuracyRadius)
        val squareR1 = radius1 * radius1
        val squareR2 = radius2 * radius2
        val squareDistance = distance * distance

        GlobalLogger.shared.info(null, "SquareDistance is: $squareDistance")

        val intersectArea = if ((radius1 + radius2) <= distance) {
            0.0
        } else {
            if ((radius1 - radius2) >= distance) {
                GlobalLogger.shared.debug(null, "Full inclusion: distance is $distance")
                PI * squareR2
            } else {
                val diameter1 = ((squareR1 - squareR2) + squareDistance).div(2 * distance)
                val diameter2 = ((squareR2 - squareR1) + squareDistance).div(2 * distance)

                val cosinus1 = max(min(diameter1.div(radius1), 1.0), -1.0)
                val cosinus2 = max(min(diameter2.div(radius2), 1.0), -1.0)

                val aera1 =
                    squareR1 * acos(cosinus1) - diameter1 * sqrt(abs(squareR1 - diameter1 * diameter1))
                val aera2 =
                    squareR2 * acos(cosinus2) - diameter2 * sqrt(abs(squareR2 - diameter2 * diameter2))

                abs(aera1 + aera2)
            }
        }

        GlobalLogger.shared.info(null, "IntersectArea value is: $intersectArea")

        val result = min(1.0, intersectArea.div(PI * accuracyRadius * accuracyRadius))
        GlobalLogger.shared.debug(
            null,
            "Inside computeConfidence: $result for zone ${zone.hash}, radius: ${zone.radius}, accuracy: $accuracyRadius, location: $location"
        )

        return result
    }
}