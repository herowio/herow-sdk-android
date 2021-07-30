package io.herow.sdk.detection.network

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.databaseModuleTest
import io.herow.sdk.detection.dispatcherModule
import io.herow.sdk.detection.geofencing.model.toLocationMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CacheWorkerTest : AutoCloseKoinTest() {

    private val ioDispatcher: CoroutineDispatcher by inject()

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: CacheWorker
    private lateinit var location: Location
    private val rennesGeohash: String = "gbwc"
    private val mockLocation = MockLocation()

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(databaseModuleTest, dispatcherModule)
        }

        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        sessionHolder.saveOptinValue(true)
        sessionHolder.saveSDKID("test")

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        location = mockLocation.buildLocation()

        val locationMapper = location.toLocationMapper(location)

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
    }

    @Test
    fun testLaunchCacheWorker() = runBlocking {

        var result = withContext(ioDispatcher) {
            worker.doWork()
        }

        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())

        sessionHolder.updateCache(true)

        result = withContext(ioDispatcher) {
            worker.doWork()
        }
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))

        sessionHolder.saveGeohash("123a")

        result = withContext(ioDispatcher) {
            worker.doWork()
        }
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))

        sessionHolder.saveOptinValue(false)

        result = withContext(ioDispatcher) {
            worker.doWork()
        }
        assertThat(result, Is.`is`(ListenableWorker.Result.failure()))

    }

    @After
    fun cleanUp() {
        stopKoin()
    }
}

