package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.helpers.LogsHelper
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class LogWorkerTest {
    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: DataHolder
    private lateinit var worker: LogsWorker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        worker = TestListenableWorkerBuilder<LogsWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    LogsWorker.KEY_LOGS to LogsHelper.createTestLogs()
                )
            )
            .build()
    }

    @Test
    fun testLogsHasBeenSent() {
        sessionHolder.saveOptinValue(true)

        runBlocking {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun testIfOptinFalseLogWontSend() {
        runBlocking {
            val result = worker.doWork()
            MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
        }
    }
}