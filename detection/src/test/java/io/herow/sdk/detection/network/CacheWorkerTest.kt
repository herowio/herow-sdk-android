package io.herow.sdk.detection.network

import android.content.Context
import android.location.Location
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.geofencing.model.toLocationMapper
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CacheWorkerTest : KoinTest, ICustomKoinTestComponent {

    private val testDispatcher = TestCoroutineDispatcher()
    private val sessionHolder: SessionHolder by inject()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var worker: CacheWorker
    private lateinit var location: Location
    private val rennesGeohash: String = "gbwc"
    private val treffendelGeohash: String = "gbw9"
    private val mockLocation = MockLocation()

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()

        sessionHolder.saveOptinValue(true)
        sessionHolder.saveSDKID("test")

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        location = mockLocation.buildLocation()

        val locationMapper = toLocationMapper(location)

        worker = TestListenableWorkerBuilder<CacheWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    CacheWorker.KEY_GEOHASH to rennesGeohash,
                    Constants.LOCATION_DATA to Gson().toJson(locationMapper)
                )
            ).build()

        worker.testing = true
    }

    @Test
    fun testLaunchCacheWorker(): Unit = testDispatcher.runBlockingTest {
        var result = worker.doWork()

        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        Thread.sleep(1000)
        Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())

        sessionHolder.updateCache(true)

        result = worker.doWork()

        assertThat(result, Is.`is`(ListenableWorker.Result.success()))

        sessionHolder.saveGeohash("123a")

        result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))

        sessionHolder.saveOptinValue(false)

        result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
    }
}

