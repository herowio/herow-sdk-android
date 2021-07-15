package io.herow.sdk.detection.geofencing

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Zone
import kotlin.math.*

data class GeofenceEvent(
    val zone: Zone,
    val location: Location,
    val type: GeofenceType,
    var confidence: Double = -1.0
) {

    fun computeEnterConfidence(location: Location, zone: Zone): Double {
        zone.radius = zone.radius ?: 0.0
        val confidence = computeConfidence(location, zone)
        GlobalLogger.shared.debug(null, "ComputeEnter: $location & $zone")
        GlobalLogger.shared.debug(null, "GeofenceEvent enter zone confidence: $confidence")

        return roundTo(confidence)
    }

    fun computeNotificationConfidence(location: Location, zone: Zone): Double {
        zone.radius = 3.times(zone.radius ?: 0.0)
        val confidence = computeConfidence(location, zone)
        GlobalLogger.shared.debug(null, "ComputeNotification: $location & $zone")
        GlobalLogger.shared.debug(
            null,
            "GeofenceEvent enter notification zone confidence: $confidence"
        )

        return roundTo(confidence)
    }

    fun computeExitConfidence(location: Location, zone: Zone): Double {
        zone.radius = zone.radius ?: 0.0
        val confidence = 1 - computeConfidence(location, zone)
        GlobalLogger.shared.debug(null, "GeofenceEvent exit zone confidence: $confidence")

        return roundTo(confidence)
    }

    private fun computeConfidence(location: Location, zone: Zone): Double {
        GlobalLogger.shared.info(null, "GeofenceEvent -  computeConfidence Parameters are: $location and $zone")

        val center = Location("").apply {
            latitude = zone.lat!!
            longitude = zone.lng!!
        }

        val distance: Double = center.distanceTo(location).toDouble()
        val accuracyRadius = location.accuracy.toDouble()
        val accuracyArea = PI * accuracyRadius * accuracyRadius

        if (accuracyRadius == 0.0) {
            GlobalLogger.shared.info(null, "GeofenceEvent -  computeConfidence Accuracy null")
            return -1.0
        }
        GlobalLogger.shared.info(
            null,
            " computeConfidence Value of distance is: $distance and value of accuracyRadius is: $accuracyRadius"
        )

        val radius1 = max(zone.radius!!, accuracyRadius)
        val radius2 = min(zone.radius!!, accuracyRadius)
        val squareR1 = radius1 * radius1
        val squareR2 = radius2 * radius2
        val squareDistance = distance * distance

        GlobalLogger.shared.info(null, " computeConfidence SquareDistance is: $squareDistance")

        val intersectArea = if ((radius1 + radius2) <= distance) {
            0.0
        } else {
            if ((radius1 - radius2) >= distance) {
                GlobalLogger.shared.debug(null, "computeConfidence Full inclusion: distance is $distance")
                PI * squareR2
            } else {
                val diameter1 = ((squareR1 - squareR2) + squareDistance) / (2 * distance)
                val diameter2 = ((squareR2 - squareR1) + squareDistance) / (2 * distance)

                val cosinus1 = max(min(diameter1 / radius1, 1.0), -1.0)
                val cosinus2 = max(min(diameter2 / radius2, 1.0), -1.0)

                val aera1 =
                    squareR1 * acos(cosinus1) - diameter1 * sqrt(abs(squareR1 - diameter1 * diameter1))
                val aera2 =
                    squareR2 * acos(cosinus2) - diameter2 * sqrt(abs(squareR2 - diameter2 * diameter2))

                abs(aera1 + aera2)
            }
        }

        GlobalLogger.shared.info(null, "computeConfidence result  IntersectArea value is: $intersectArea")
        GlobalLogger.shared.info(null, "computeConfidence result AccuracyArera value is: $accuracyArea")

        val ratio: Double = intersectArea / accuracyArea
        GlobalLogger.shared.info(null, "computeConfidence result ratio value is: $ratio")

        val result = min(1.0, ratio)
        GlobalLogger.shared.debug(
            null,
            "Inside computeConfidence: $result for zone ${zone.hash}, radius: ${zone.radius}, accuracy: $accuracyRadius, location: $location"
        )

        return result
    }

    private fun roundTo(data: Double): Double {
        val factor = 100.0.pow(data)

        return (data * factor).roundToInt() / factor
    }
}