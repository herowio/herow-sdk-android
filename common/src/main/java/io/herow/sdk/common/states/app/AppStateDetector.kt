package io.herow.sdk.common.states.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Official solution provides by Google to be able to detect the application switching between
 * foreground and background state.
 * @see: https://developer.android.com/reference/androidx/lifecycle/ProcessLifecycleOwner.html
 * @see: https://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo/44461605#44461605
 */
class AppStateDetector: LifecycleObserver {
    companion object {
        fun addAppStateListener(appStateListener: AppStateListener) {
            appStateListeners.add(appStateListener)
        }

        private val appStateListeners = CopyOnWriteArrayList<AppStateListener>()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        for (appStateListener in appStateListeners) {
            appStateListener.onAppInForeground()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        for (appStateListener in appStateListeners) {
            appStateListener.onAppInBackground()
        }
    }
}