package io.herow.sdk.common.states.motion

import android.content.Intent
import android.os.SystemClock
import com.google.android.gms.common.internal.safeparcel.SafeParcelableSerializer
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

object ActivityTransitionBuilder {
    private const val ACTIVITY_TRANSITION_ACTION = "com.google.android.location.internal.EXTRA_ACTIVITY_TRANSITION_RESULT"

    fun buildReceivedIntent(): Intent {
        val intent = Intent()
        val events: MutableList<ActivityTransitionEvent> = ArrayList()
        events.add(
            ActivityTransitionEvent(
                DetectedActivity.STILL,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT,
                SystemClock.elapsedRealtimeNanos()
            )
        )
        events.add(
            ActivityTransitionEvent(
                DetectedActivity.WALKING,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                SystemClock.elapsedRealtimeNanos()
            )
        )
        val safeParcelable = SafeParcelableSerializer.serializeToBytes(
            ActivityTransitionResult(
                events
            )
        )
        intent.putExtra(ACTIVITY_TRANSITION_ACTION, safeParcelable)
        return intent
    }
}