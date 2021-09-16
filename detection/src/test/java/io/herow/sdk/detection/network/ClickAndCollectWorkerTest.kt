package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.clickandcollect.ClickAndCollectDispatcher
import io.herow.sdk.detection.clickandcollect.ClickAndCollectWorker
import io.herow.sdk.detection.clickandcollect.IClickAndCollectListener
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ClickAndCollectWorkerTest : KoinTest, ICustomKoinTestComponent {
    private val ioDispatcher: CoroutineDispatcher by inject()

    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var worker: ClickAndCollectWorker
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var sessionHolder: SessionHolder
    private lateinit var clickAndCollectWorkerListener: ClickAndCollectWorkerListener
    private lateinit var herowInitializer: HerowInitializer

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        herowInitializer = HerowInitializer.getInstance(context, true)
        TimeHelper.testing = true
        DeviceHelper.testing = true

        worker = TestListenableWorkerBuilder<ClickAndCollectWorker>(context)
            .build()

        clickAndCollectWorkerListener = ClickAndCollectWorkerListener()

        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)
    }

    @Test
    fun testLaunchDoWork() = runBlocking {
        withContext(ioDispatcher) {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }
    }


    @Test
    fun testOnceFinishClickAndCollectShouldNotBeProgression() {
        ClickAndCollectDispatcher.registerClickAndCollectListener(clickAndCollectWorkerListener)
        Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "click")

        runBlocking {
            withContext(ioDispatcher) {
                val result = worker.doWork()
                MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
                Assert.assertFalse(sessionHolder.getClickAndCollectProgress())
            }
        }
    }

    @Test
    fun testOnCancellationShouldDisplayStop() {
        ClickAndCollectDispatcher.registerClickAndCollectListener(clickAndCollectWorkerListener)
        Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "click")

        runBlocking {
            withContext(ioDispatcher) {
                println("into onCancellation")
                val job = launch {
                    worker.doWork()
                }

                delay(1000)
                job.cancel(CancellationException())
                delay(1000)
            }
            Assert.assertEquals(clickAndCollectWorkerListener.variableTest, "stop")
            Assert.assertFalse(herowInitializer.isOnClickAndCollect())
        }
    }

    @After
    fun cleanUp() {
        ClickAndCollectDispatcher.unregisterClickAndCollectListener(clickAndCollectWorkerListener)
    }
}

class ClickAndCollectWorkerListener(var variableTest: String = "click") : IClickAndCollectListener {
    override fun didStopClickAndConnect() {
        variableTest = "stop"
    }

    override fun didStartClickAndConnect() {
        variableTest = "start"
    }
}