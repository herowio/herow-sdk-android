package io.herow.sdk.common.states.motion

class MyMotionEventListener: MotionEventListener {
    val motionsEvents = ArrayList<MotionEvent>()

    override fun onMotionEvent(motionsEvent: List<MotionEvent>) {
        this.motionsEvents.addAll(motionsEvent)
    }
}