package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.android.gms.common.data.DataHolder
import io.herow.sdk.connection.SessionHolder
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
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
        dataHolder = io.herow.sdk.common.DataHolder(context, "io.herow.sdk")
        sessionHolder = SessionHolder(dataHolder)
    }

    @Test
    fun testConfigWorker() {
        val worker = TestListenableWorkerBuilder<ConfigWorker>(context).build()

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            assertNotNull(sessionHolder.getAccessToken())
            assertNotNull(sessionHolder.getHerowId())
        }
    }

}