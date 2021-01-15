package io.herow.sdk.common.states.motion

data class MotionEvent(val activityType: Int,
                  val transitionType: Int,
                  val elapsedRealTimeNanos: Long)