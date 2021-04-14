package io.herow.sdk.common.states.app

/**
 * To be notified when the application switches from background to foreground or from foreground to background
 * When the application starts, the onAppInBackground method is called.
 * Next, if an activity starts, the onAppInForeground method is called.
 */
interface AppStateListener {
    fun onAppInForeground()
    fun onAppInBackground()
}