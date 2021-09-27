package io.herow.sdk.connection

import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.DataHolder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class SessionInterceptorTest {
    private val mockWebServer = MockWebServer()
    private lateinit var fakeAPI: FakeAPI
    private lateinit var sessionHolder: SessionHolder

    @Before
    fun setUp() {
        sessionHolder = SessionHolder(DataHolder(ApplicationProvider.getApplicationContext()))
        val serverURL = mockWebServer.url("/").toString()
        fakeAPI = RetrofitBuilder.buildRetrofitForAPI(sessionHolder, serverURL, FakeAPI::class.java, true)
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