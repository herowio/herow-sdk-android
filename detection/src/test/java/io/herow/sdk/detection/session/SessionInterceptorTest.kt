package io.herow.sdk.detection.session

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.livemoment.HerowInitializer
import io.herow.sdk.detection.helpers.FakeAPI
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinComponent
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class SessionInterceptorTest : KoinTest, ICustomKoinComponent {
    private val mockWebServer = MockWebServer()
    private lateinit var fakeAPI: FakeAPI

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sessionHolder: SessionHolder by inject()

    @Before
    fun setUp() {
        io.herow.sdk.livemoment.HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()

        val serverURL = mockWebServer.url("/").toString()
        fakeAPI = RetrofitBuilder.buildRetrofitForAPI(serverURL, FakeAPI::class.java, true)
    }

    @Test
    fun testRequest() = runBlocking {
        mockWebServer.enqueue(MockResponse())
        fakeAPI.getAnswerToUniverse().execute()
        val headers = mockWebServer.takeRequest().headers.names()
        Assert.assertFalse(headers.contains(HerowHeaders.AUTHORIZATION_HEADER))
        Assert.assertFalse(headers.contains(HerowHeaders.DEVICE_ID_HEADER))
        Assert.assertFalse(headers.contains(HerowHeaders.HEROW_ID_HEADER))

        sessionHolder.saveAccessToken(UUID.randomUUID().toString())
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        sessionHolder.saveHerowId(UUID.randomUUID().toString())

        mockWebServer.enqueue(MockResponse())
        fakeAPI.getAnswerToUniverse().execute()
        val newHeaders = mockWebServer.takeRequest().headers.names()

        Assert.assertTrue(newHeaders.contains(HerowHeaders.AUTHORIZATION_HEADER))
        Assert.assertTrue(newHeaders.contains(HerowHeaders.DEVICE_ID_HEADER))
        Assert.assertTrue(newHeaders.contains(HerowHeaders.HEROW_ID_HEADER))
    }
}