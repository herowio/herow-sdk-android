package io.herow.sdk.common.states.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class AppStateDetectorTest {
    private val appStateListener = MyListener()
    private val lifecycleRegistry = LifecycleRegistry(Mockito.mock(LifecycleOwner::class.java))

    @Before
    fun setUp() {
        val appStateDetector = AppStateDetector()
        lifecycleRegistry.addObserver(appStateDetector)
        AppStateDetector.addAppStateListener(appStateListener)
    }

    @Test
    fun testAppStates() {
        Assert.assertFalse(appStateListener.isOnForeground)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        Assert.assertTrue(appStateListener.isOnForeground)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        Assert.assertFalse(appStateListener.isOnForeground)
    }

    class MyListener: AppStateListener {
        var isOnForeground: Boolean = false

        override fun onAppInForeground() {
            isOnForeground = true
        }

        override fun onAppInBackground() {
            isOnForeground = false
        }
    }
}