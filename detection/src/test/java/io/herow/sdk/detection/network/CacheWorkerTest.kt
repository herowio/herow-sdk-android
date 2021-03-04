package io.herow.sdk.detection.network

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.CacheDispatcher
import io.herow.sdk.connection.cache.CacheListener
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.helpers.GeoHashHelper
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CacheWorkerTest {
    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: CacheWorker
    private lateinit var location: Location

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        sessionHolder.saveOptinValue(true)

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        location = MockLocation.buildLocation()

        worker = TestListenableWorkerBuilder<CacheWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    CacheWorker.KEY_GEOHASH to GeoHashHelper.encodeBase32(location)
                )
            ).build()
    }

    @Test
    fun testLaunchTokenCacheWorker() {
        runBlocking {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        }
    }

    @Test
    fun testLaunchUserInfoCacheWorker() {
        runBlocking {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
        }
    }

    @Test
    fun testLaunchCacheWorker() {
        val cacheWorkerListener = CacheWorkerListener()
        CacheDispatcher.addCacheListener(cacheWorkerListener)
        Assert.assertNull(cacheWorkerListener.cacheResult)

        runBlocking {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertNotNull(cacheWorkerListener.cacheResult)
        }
    }

    @Test
    fun testCacheIsCalledIfUpdateStatusTrue() {
        sessionHolder.updateCache(true)

        val cacheWorkerListener = CacheWorkerListener()
        CacheDispatcher.addCacheListener(cacheWorkerListener)
        Assert.assertNull(cacheWorkerListener.cacheResult)

        runBlocking {
            worker.doWork()
            Assert.assertNotNull(cacheWorkerListener.cacheResult)
        }
    }

    @Test
    fun testCacheIsCalledIfUpdateStatusTrueAndGeoHashIsKnownAndDifferent() {
        sessionHolder.updateCache(true)
        sessionHolder.saveGeohash("123a")

        val cacheWorkerListener = CacheWorkerListener()
        CacheDispatcher.addCacheListener(cacheWorkerListener)
        Assert.assertNull(cacheWorkerListener.cacheResult)

        runBlocking {
            worker.doWork()
            Assert.assertNotNull(cacheWorkerListener.cacheResult)
        }
    }

    @Test
    fun testCacheIsNotCalledIfUpdateStatusFalseAndGeoHashIsNotDifferent() {
        sessionHolder.updateCache(false)
        sessionHolder.saveGeohash(GeoHashHelper.encodeBase32(location))

        val cacheWorkerListener = CacheWorkerListener()
        CacheDispatcher.addCacheListener(cacheWorkerListener)
        Assert.assertNull(cacheWorkerListener.cacheResult)

        runBlocking {
            worker.doWork()
            Assert.assertNull(cacheWorkerListener.cacheResult)
        }
    }

    @Test
    fun testDoWorkIsNotCalledIfOptinIsFalse() {
        sessionHolder.saveOptinValue(false)
        
        runBlocking {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
        }
    }

}

class CacheWorkerListener(var cacheResult: CacheResult? = null) : CacheListener {
    override fun onCacheReception(cacheResult: CacheResult) {
        this.cacheResult = cacheResult
    }
}

