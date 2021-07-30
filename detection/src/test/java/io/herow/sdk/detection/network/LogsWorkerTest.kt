package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.databaseModuleTest
import io.herow.sdk.detection.dispatcherModule
import io.herow.sdk.detection.helpers.LogsHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class LogsWorkerTest: KoinTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: DataHolder
    private lateinit var worker: LogsWorker

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(databaseModuleTest, dispatcherModule)
        }

        context = ApplicationProvider.getApplicationContext()
        dataHolder = DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)
        sessionHolder.saveSDKID("test")

        worker = TestListenableWorkerBuilder<LogsWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    LogsWorker.KEY_LOGS to LogsHelper(sessionHolder).createTestLogs(context)
                )
            )
            .build()
    }

    @Test
    fun testLogsHasBeenSent() = runBlocking {
        sessionHolder.saveOptinValue(true)

        val result = worker.doWork()
        MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.success()))
    }

    @Test
    fun testIfOptinFalseLogWontSend() = runBlocking {
        val result = worker.doWork()
        MatcherAssert.assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
    }

    @After
    fun cleanUp() {
        stopKoin()
    }
}