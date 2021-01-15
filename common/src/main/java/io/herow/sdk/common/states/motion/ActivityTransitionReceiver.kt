package io.herow.sdk.common.states.motion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import io.herow.sdk.common.states.app.AppStateDetector
import io.herow.sdk.common.states.app.AppStateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class ActivityTransitionReceiver: BroadcastReceiver() {
    companion object {
        fun addMotionEventListener(motionEventListener: MotionEventListener) {
            motionEventListeners.add(motionEventListener)
        }
        private val motionEventListeners = CopyOnWriteArrayList<MotionEventListener>()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        println("onReceive")
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult.extractResult(intent)?.let { result: ActivityTransitionResult ->
                val motionsEvent = ArrayList<MotionEvent>()
                for (event in result.transitionEvents) {
                    val motionEvent = MotionEvent(event.activityType, event.transitionType, event.elapsedRealTimeNanos)
                    motionsEvent.add(motionEvent)
                }
                for (motionEventListener in motionEventListeners) {
                    motionEventListener.onMotionEvent(motionsEvent)
                }
            }
        }
    }
}