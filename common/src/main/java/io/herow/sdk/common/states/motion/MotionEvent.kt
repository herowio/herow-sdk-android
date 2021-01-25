package io.herow.sdk.common.states.motion

import com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
import io.herow.sdk.common.helpers.TimeHelper

data class MotionEvent(val activityType: Int,
                       val transitionType: Int,
                       val elapsedRealTimeNanos: Long) {
    companion object {
        fun default(): MotionEvent {
            val motionType = MotionType.getDetectedActivity(MotionType.UNKNOWN)
            return MotionEvent(motionType, ACTIVITY_TRANSITION_ENTER, TimeHelper.getCurrentTime())
        }
    }
}