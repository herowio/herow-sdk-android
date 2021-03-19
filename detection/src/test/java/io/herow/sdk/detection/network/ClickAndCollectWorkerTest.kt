package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.clickandcollect.ClickAndCollectDispatcher
import io.herow.sdk.detection.clickandcollect.ClickAndCollectListener
import io.herow.sdk.detection.clickandcollect.ClickAndCollectWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ClickAndCollectWorkerTest {
    private lateinit var context: Context
    private lateinit var worker: ClickAndCollectWorker

    private lateinit var clickAndCollectWorkerListener: ClickAndCollectWorkerListener

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        worker = TestListenableWorkerBuilder<ClickAndCollectWorker>(context)
            .build()

        clickAndCollectWorkerListener = ClickAndCollectWorkerListener()
    }

    @Test
    fun testLaunchDoWork() {
        runBlocking {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun testDidStartClickAndConnectShouldDisplayStart() {
        ClickAndCollectDispatcher.registerClickAndCollectListener(clickAndCollectWorkerListener)
        Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "click")

        runBlocking {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertTrue(HerowInitializer.getInstance(context).isOnClickAndCollect())
            Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "start")
        }
    }

    @Test
    fun testOnCancellationShouldDisplayStop() {
        ClickAndCollectDispatcher.registerClickAndCollectListener(clickAndCollectWorkerListener)
        Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "click")

        runBlocking {
            val job = launch {
                worker.doWork()
            }

            delay(1000)
            job.cancel(CancellationException())
            delay(1000)
            Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "stop")
            Assert.assertFalse(HerowInitializer.getInstance(context).isOnClickAndCollect())
        }
    }

    @After
    fun cleanUp() {
        ClickAndCollectDispatcher.unregisterClickAndCollectListener(clickAndCollectWorkerListener)
    }
}

class ClickAndCollectWorkerListener(var variableTest: String = "click") : ClickAndCollectListener {
    override fun didStopClickAndConnect() {
        variableTest = "stop"
    }

    override fun didStartClickAndConnect() {
        variableTest = "start"
    }
}