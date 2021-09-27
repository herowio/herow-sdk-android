package io.herow.sdk.detection.network

import android.content.Context
import android.location.Location
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.base.Verify.verify
import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.MockLocation
import io.herow.sdk.detection.geofencing.model.LocationMapper
import io.herow.sdk.detection.geofencing.model.toLocationMapper
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mock
import org.robolectric.annotation.Config
import java.net.UnknownHostException
import java.util.*

@Config(sdk = [28])
class HerowAPITest(private val context: Context): KoinTest, ICustomKoinTestComponent {
    private val ioDispatcher: CoroutineDispatcher by inject()
    //private var context: Context = PowerMockito.mock(Context::class.java)

    private val rennesGeohash: String = "gbwc"
    private lateinit var location: Location
    private val mockLocation = MockLocation()
    private lateinit var locationMapper: LocationMapper

    private lateinit var dataHolder: DataHolder
    private lateinit var sessionHolder: SessionHolder

    @Mock
    private lateinit var herowInitializer: HerowInitializer

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)

        //herowInitializer = PowerMockito.mock(HerowInitializer::class.java)
        herowInitializer = HerowInitializer.getInstance(context, true)

        dataHolder = DataHolder(context)

        //dataHolder = DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)
        sessionHolder.saveOptinValue(true)
        sessionHolder.saveSDKID("test")

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())

        location = mockLocation.buildLocation()

        locationMapper = location.toLocationMapper(location)
    }

    @After
    fun cleanUp() {
        herowInitializer.reset()
    }

    @Test
    fun testSimple() {
        val word = "cat"
        verify(word == "cat")
    }

    @Test
    fun testWorkerWithPreProdURL(): Unit = runBlocking {
        withContext(ioDispatcher) {
            println("HerowPlatform is: ${HerowPlatform.PRE_PROD}")
            herowInitializer
                .configApp(NetworkConstants.USERNAME, NetworkConstants.PASSWORD)
                .configPlatform(HerowPlatform.PRE_PROD)
                .synchronize()

            val worker = TestListenableWorkerBuilder<CacheWorker>(context)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                        AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                        AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                        AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                        CacheWorker.KEY_GEOHASH to rennesGeohash,
                        Constants.LOCATION_DATA to Gson().toJson(locationMapper)
                    )
                ).build()

            worker.testing = true

            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))

            verify(sessionHolder.getCustomPreProdURL() == Constants.DEFAULT_PRE_PROD_URL)

            herowInitializer.setPreProdCustomURL("https://chat.chien.io")
            println(" Herow Initializer is: ${herowInitializer.getData()}")
            println("Data saved: ${sessionHolder.getAllValues()}")

            println("Platform in PreProd is: ${sessionHolder.getPlatformName()}")
            println("PreProd URL is: ${sessionHolder.getCustomPreProdURL()}")
            Assert.assertFalse(sessionHolder.getCustomPreProdURL() == Constants.DEFAULT_PRE_PROD_URL)
            Assert.assertTrue(sessionHolder.getCustomPreProdURL() == "https://chat.chien.io")

            try {
                worker.doWork()
            } catch (exception: UnknownHostException) {
                assertThat(exception.message, Is.`is`("chat.chien.io: nodename nor servname provided, or not known"))
            }
        }
    }

    @Test
    fun testWorkerWithProdURL(): Unit = runBlocking {
        println("Herow is: $herowInitializer")

        withContext(ioDispatcher) {
            herowInitializer
                .configApp(NetworkConstants.USERNAME, NetworkConstants.PASSWORD)
                .configPlatform(HerowPlatform.PROD)
                .synchronize()

            val anotherWorker = TestListenableWorkerBuilder<CacheWorker>(context)
                .setInputData(
                    workDataOf(
                        AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                        AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                        AuthRequests.KEY_PLATFORM to HerowPlatform.PROD.name,
                        AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                        CacheWorker.KEY_GEOHASH to rennesGeohash,
                        Constants.LOCATION_DATA to Gson().toJson(locationMapper)
                    )
                ).build()

            anotherWorker.testing = true

            val result = anotherWorker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getCustomProdURL() == Constants.DEFAULT_PROD_URL)

            herowInitializer.setProdCustomURL("https://poule.canard.io")

            synchronized(sessionHolder) {
                println(" Herow Initializer is: ${herowInitializer.getData()}")
                println("Data saved: ${sessionHolder.getAllValues()}")

                println("Platform is: ${sessionHolder.getPlatformName()}")
                println("Prod URL is: ${sessionHolder.getCustomProdURL()}")
                Assert.assertFalse(sessionHolder.getCustomProdURL() == Constants.DEFAULT_PROD_URL)
                Assert.assertTrue(sessionHolder.getCustomProdURL() == "https://poule.canard.io")
            }
        }
    }
}