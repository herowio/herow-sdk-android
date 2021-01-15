package io.herow.sdk.common.states.motion

/**
 * To be notified when the user is changing his current activity. For example: when running, you will
 * receive this callback.
 * @see ActivityTransitionDetector
 * @see ActivityTransitionReceiver
 */
interface MotionEventListener {
    fun onMotionEvent(motionsEvent: List<MotionEvent>)
}