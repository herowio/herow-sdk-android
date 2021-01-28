package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigListener
import io.herow.sdk.connection.config.ConfigResult
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ConfigWorkerTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: ConfigWorker

    private val username = "test"
    private val password = "test"
    private val customID = "randomCustom"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        worker = TestListenableWorkerBuilder<ConfigWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to username,
                    AuthRequests.KEY_SDK_KEY to password,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                    AuthRequests.KEY_CUSTOM_ID to customID
                )
            ).build()
    }

    @Test
    fun testLaunchTokenConfigWorker() {
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        }
    }

    @Test
    fun testLaunchUserInfoConfigWorker() {
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
        }
    }

    @Test
    fun testConfigResult() {
        val configWorkerListener = ConfigWorkerListener()
        ConfigDispatcher.addConfigListener(configWorkerListener)
        Assert.assertNull(configWorkerListener.configResult)

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertNotNull(configWorkerListener.configResult)
        }

    }
}

class ConfigWorkerListener(var configResult: ConfigResult? = null) : ConfigListener {
    override fun onConfigResult(configResult: ConfigResult) {
        this.configResult = configResult
    }
}