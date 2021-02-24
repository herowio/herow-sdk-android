package io.herow.sdk.common.states.motion

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ActivityTransitionReceiverTest {
    private val activityTransitionReceiver = ActivityTransitionReceiver()
    private val myMotionEventListener = MyMotionEventListener()

    @Before
    fun setUp() {
        ActivityTransitionReceiver.addMotionEventListener(myMotionEventListener)
    }

    @Test
    fun testOnReceive() {
        Assert.assertTrue(myMotionEventListener.motionsEvents.isEmpty())
        val context: Context = ApplicationProvider.getApplicationContext()
        activityTransitionReceiver.onReceive(context, ActivityTransitionBuilder.buildReceivedIntent())
        Assert.assertTrue(myMotionEventListener.motionsEvents.isNotEmpty())
    }

    @Test
    fun testOnReceiveEmptyIntent() {
        Assert.assertTrue(myMotionEventListener.motionsEvents.isEmpty())
        val context: Context = ApplicationProvider.getApplicationContext()
        activityTransitionReceiver.onReceive(context, Intent())
        Assert.assertTrue(myMotionEventListener.motionsEvents.isEmpty())
    }
}