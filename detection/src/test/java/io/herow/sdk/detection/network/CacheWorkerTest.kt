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
import io.herow.sdk.detection.helpers.GeoHashHelper
import kotlinx.coroutines.runBlocking
import org.bouncycastle.crypto.prng.RandomGenerator
import org.hamcrest.MatcherAssert
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

        location = Location(sessionHolder.getDeviceId()).apply {
            latitude = 48.11705624819015
            longitude = -1.6757520432921995
        }

        worker = TestListenableWorkerBuilder<CacheWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to username,
                    AuthRequests.KEY_SDK_KEY to password,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                    AuthRequests.KEY_CUSTOM_ID to customID,
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


}

class CacheWorkerListener(var cacheResult: CacheResult? = null) : CacheListener {
    override fun onCacheReception(cacheResult: CacheResult) {
        this.cacheResult = cacheResult
    }
}

