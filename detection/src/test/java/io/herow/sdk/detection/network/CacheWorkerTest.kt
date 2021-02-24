package io.herow.sdk.detection.network

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.CoroutineTestRule
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.helpers.DispatcherProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CacheWorkerTest {
    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: CacheWorker
    private lateinit var location: Location
    private lateinit var db: HerowDatabase

    private lateinit var zoneRepository: ZoneRepository
    private lateinit var poiRepository: PoiRepository
    private var zones: List<Zone>? = null
    private var pois: List<Poi>? = null

    private val RENNES_GEOHASH: String = "gbwc"

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        db = HerowDatabase.getDatabase(context)
        zoneRepository = ZoneRepository(db.zoneDAO())
        poiRepository = PoiRepository(db.poiDAO())

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        location = MockLocation(context).buildLocation()

        worker = TestListenableWorkerBuilder<CacheWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    CacheWorker.KEY_GEOHASH to RENNES_GEOHASH
                    //CacheWorker.KEY_GEOHASH to GeoHashHelper.encodeBase32(location)
                )
            )
            .build()

    }

    @Test
    fun testLaunchTokenCacheWorker() = coroutineTestRule.testDispatcher.runBlockingTest {
        CoroutineScope(Dispatchers.IO).launch {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        }
    }


    @Test
    fun testLaunchUserInfoCacheWorker() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
        }
    }

    @Test
    fun testLaunchCacheWorker() = coroutineTestRule.testDispatcher.runBlockingTest {
        CoroutineScope(Dispatchers.IO).launch {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun testCacheResultIsSavedInDB() = coroutineTestRule.testDispatcher.runBlockingTest {
        CoroutineScope(Dispatchers.IO).launch {
            db.runInTransaction {
                zones = zoneRepository.getAllZones()
                pois = poiRepository.getAllPois()
            }

            Assert.assertTrue(zones.isNullOrEmpty())
            Assert.assertTrue(pois.isNullOrEmpty())

            worker.doWork()
            zones = zoneRepository.getAllZones()
            pois = poiRepository.getAllPois()
            Assert.assertTrue(!zones.isNullOrEmpty())
            Assert.assertTrue(!pois.isNullOrEmpty())
        }
    }


    @Test
    fun testCacheIsCalledIfUpdateStatusTrue() = coroutineTestRule.testDispatcher.runBlockingTest {
        sessionHolder.updateCache(true)

        CoroutineScope(Dispatchers.IO).launch {
            zones = zoneRepository.getAllZones()
            Assert.assertTrue(zones.isNullOrEmpty())

            worker.doWork()
            zones = zoneRepository.getAllZones()
            Assert.assertTrue(!zones.isNullOrEmpty())
        }
    }

    @Test
    fun testCacheIsCalledIfUpdateStatusTrueAndGeoHashIsKnownAndDifferent() =
        coroutineTestRule.testDispatcher.runBlockingTest {
            sessionHolder.updateCache(true)
            sessionHolder.saveGeohash("123a")

            CoroutineScope(Dispatchers.IO).launch {
                zones = zoneRepository.getAllZones()
                Assert.assertTrue(zones.isNullOrEmpty())

                worker.doWork()
                zones = zoneRepository.getAllZones()
                Assert.assertTrue(!zones.isNullOrEmpty())
            }
        }

    @Test
    fun testCacheIsNotCalledIfUpdateStatusFalseAndGeoHashIsNotDifferent() {
        CoroutineScope(Dispatchers.IO).launch {
            sessionHolder.updateCache(false)
            sessionHolder.saveGeohash(RENNES_GEOHASH)

            CoroutineScope(Dispatchers.IO).launch {
                zones = zoneRepository.getAllZones()
                Assert.assertTrue(zones.isNullOrEmpty())

                worker.doWork()
                zones = zoneRepository.getAllZones()
                Assert.assertTrue(zones.isNullOrEmpty())
            }
        }
    }

    @After
    fun close() {
        coroutineTestRule.testDispatcher.cleanupTestCoroutines()
        db.close()
    }
}

