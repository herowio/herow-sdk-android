package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.android.gms.common.data.DataHolder
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ConfigWorkerTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)
    }

    @Test
    fun testConfigWorker() {
        val worker = TestListenableWorkerBuilder<ConfigWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to "test",
                    AuthRequests.KEY_SDK_KEY to "test",
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name
                )
            ).build()

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
            Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
        }
    }

}