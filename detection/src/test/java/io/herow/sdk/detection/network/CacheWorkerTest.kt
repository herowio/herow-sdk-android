package io.herow.sdk.detection.network

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.MockLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CacheWorkerTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: CacheWorker
    private lateinit var location: Location
    private lateinit var db: HerowDatabase

    private lateinit var zoneRepository: ZoneRepository
    private lateinit var poiRepository: PoiRepository

    private val rennesGeohash: String = "gbwc"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        sessionHolder.saveOptinValue(true)
        sessionHolder.saveSDKID("test")
        db = HerowDatabase.getDatabase(context)
        zoneRepository = ZoneRepository(db.zoneDAO())
        poiRepository = PoiRepository(db.poiDAO())

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        location = MockLocation(context).buildLocation()

        val cacheWorkerFactory = CacheWorkerFactory()

        worker = TestListenableWorkerBuilder<CacheWorker>(
            context, inputData = workDataOf(
                AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name,
                AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                CacheWorker.KEY_GEOHASH to rennesGeohash
            )
        )
            .setWorkerFactory(cacheWorkerFactory)
            .build()
    }

    @Test
    fun testLaunchTokenCacheWorker() = runBlocking {
        val result = worker.doWork()

        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
    }

    @Test
    fun testLaunchUserInfoCacheWorker() = runBlocking {
        val result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
    }


    @Test
    fun testLaunchCacheWorker() = runBlocking {
        val result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
    }

    @Test
    fun testCacheIsCalledIfUpdateStatusTrue() = runBlocking {
        sessionHolder.updateCache(true)

        val result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
    }

    @Test
    fun testCacheIsCalledIfUpdateStatusTrueAndGeoHashIsKnownAndDifferent() =
        runBlocking {
            sessionHolder.updateCache(true)
            sessionHolder.saveGeohash("123a")

            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }

    @Test
    fun testWorkerIsNotCalledIfOptinIsFalse() = runBlocking {
        sessionHolder.saveOptinValue(false)

        val result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
    }

    @After
    fun close() {
        db.close()
    }
}

