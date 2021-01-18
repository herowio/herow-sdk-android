package io.herow.sdk.common.states.motion

import com.google.android.gms.location.DetectedActivity

/**
 * We use this enumeration to avoid to use the API integer constant.
 * IN_VEHICLE: the device is in a vehicle, such as a car
 * ON_BICYCLE: the device is on a bicycle
 * ON_FOOT: the device is on a user who is walking or running
 * RUNNING
 * STILL: the device is not moving
 * TILTING: the device angle relative to gravity changed significantly
 * UNKNOWN
 * WALKING
 */
enum class MotionType {
    IN_VEHICLE,
    ON_BICYCLE,
    ON_FOOT,
    RUNNING,
    STILL,
    TILTING,
    UNKNOWN,
    WALKING;

    companion object {
        fun getType(detectedActivity: Int): MotionType {
            return when (detectedActivity) {
                DetectedActivity.IN_VEHICLE -> IN_VEHICLE
                DetectedActivity.ON_BICYCLE -> ON_BICYCLE
                DetectedActivity.ON_FOOT -> ON_FOOT
                DetectedActivity.STILL -> STILL
                DetectedActivity.TILTING -> TILTING
                DetectedActivity.WALKING -> WALKING
                DetectedActivity.RUNNING -> RUNNING
                else -> UNKNOWN
            }
        }

        fun getType(detectedActivity: DetectedActivity): MotionType {
            return getType(detectedActivity.type)
        }

        fun getDetectedActivity(motionType: MotionType): Int {
            return when (motionType) {
                IN_VEHICLE -> DetectedActivity.IN_VEHICLE
                ON_BICYCLE -> DetectedActivity.ON_BICYCLE
                ON_FOOT -> DetectedActivity.ON_FOOT
                STILL -> DetectedActivity.STILL
                TILTING -> DetectedActivity.TILTING
                WALKING -> DetectedActivity.WALKING
                RUNNING -> DetectedActivity.RUNNING
                else -> DetectedActivity.UNKNOWN
            }
        }
    }
}