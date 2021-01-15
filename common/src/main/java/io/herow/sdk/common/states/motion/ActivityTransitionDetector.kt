package io.herow.sdk.common.states.motion

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest

/**
 * Offer the possibility to detect when users start or end an activity. You can for example detect
 * when a user is running or in a vehicle.
 * @see: https://developer.android.com/guide/topics/location/transitions
 */
class ActivityTransitionDetector {
    companion object {
        private val TAG = ActivityTransitionDetector::class.java.simpleName

        fun addMotionEventListener(motionEventListener: MotionEventListener) {
            ActivityTransitionReceiver.addMotionEventListener(motionEventListener)
        }
    }
    private lateinit var pendingIntent: PendingIntent

    @SuppressLint("MissingPermission")
    fun launchTransitionMonitoring(context: Context) {
        val transitionsToMonitor = buildActivitiesTransitions()
        val request = ActivityTransitionRequest(transitionsToMonitor)
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val task = ActivityRecognition.getClient(context).requestActivityTransitionUpdates(request, pendingIntent)
        task.addOnSuccessListener {
            Log.e(TAG, "TransitionRecognition is started")
        }
        task.addOnFailureListener { exception ->
            Log.e(TAG, "onFailure: " + exception.message)
        }
    }

    private fun buildActivitiesTransitions(): List<ActivityTransition> {
        val transitions = ArrayList<ActivityTransition>()
        for (motionType in MotionType.values()) {
            if (motionType !== MotionType.TILTING && motionType !== MotionType.UNKNOWN) {
                transitions.add(
                    ActivityTransition.Builder()
                        .setActivityType(MotionType.getDetectedActivity(motionType))
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
                )
                transitions.add(
                    ActivityTransition.Builder()
                        .setActivityType(MotionType.getDetectedActivity(motionType))
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
                )
            }
        }
        return transitions
    }

    @SuppressLint("MissingPermission")
    fun stopTransitionMonitoring(context: Context) {
        ActivityRecognition.getClient(context).removeActivityTransitionUpdates(pendingIntent)
            .addOnSuccessListener {
                pendingIntent.cancel()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Transitions could not be unregistered: $e")
            }
    }
}