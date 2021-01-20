package io.herow.sdk.connection

import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.IdentifiersHolder
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
    private lateinit var dataHolder: IdentifiersHolder

    @Before
    fun setUp() {
        dataHolder = IdentifiersHolder(DataHolder(ApplicationProvider.getApplicationContext()))
        val serverURL = mockWebServer.url("/").toString()
        fakeAPI = RetrofitBuilder.buildRetrofitForAPI(dataHolder, serverURL, FakeAPI::class.java, true)
    }

    @Test
    fun testRequest() = runBlocking {
        mockWebServer.enqueue(MockResponse())
        fakeAPI.getAnswerToUniverse().execute()
        val headers = mockWebServer.takeRequest().headers.names()
        Assert.assertFalse(headers.contains(HerowHeaders.AUTHORIZATION_HEADER))
        Assert.assertFalse(headers.contains(HerowHeaders.DEVICE_ID_HEADER))
        Assert.assertFalse(headers.contains(HerowHeaders.HEROW_ID_HEADER))

        dataHolder.saveAccessToken(UUID.randomUUID().toString())
        dataHolder.saveDeviceId(UUID.randomUUID().toString())
        dataHolder.saveHerowId(UUID.randomUUID().toString())

        mockWebServer.enqueue(MockResponse())
        fakeAPI.getAnswerToUniverse().execute()
        val newHeaders = mockWebServer.takeRequest().headers.names()
        Assert.assertTrue(newHeaders.contains(HerowHeaders.AUTHORIZATION_HEADER))
        Assert.assertTrue(newHeaders.contains(HerowHeaders.DEVICE_ID_HEADER))
        Assert.assertTrue(newHeaders.contains(HerowHeaders.HEROW_ID_HEADER))
    }
}