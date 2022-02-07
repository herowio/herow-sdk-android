package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.helpers.LogsHelper
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class LogsWorkerTest: ICustomKoinTestComponent {
    private val ioDispatcher: CoroutineDispatcher by inject()
    private val sessionHolder: SessionHolder by inject()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var worker: LogsWorker

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()

        sessionHolder.saveSDKID("test")
        worker = TestListenableWorkerBuilder<LogsWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    LogsWorker.KEY_LOGS to LogsHelper().createTestLogs(context)
                )
            )
            .build()

        worker.testing = true
    }

    @Test
    fun testLogsHasBeenSent() = runBlocking {
        sessionHolder.saveOptinValue(true)

        withContext(ioDispatcher) {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun testIfOptinFalseLogWontSend() = runBlocking {
        sessionHolder.saveOptinValue(false)
        withContext(ioDispatcher) {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
        }
    }
}