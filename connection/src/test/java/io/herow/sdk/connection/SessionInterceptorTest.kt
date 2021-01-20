package io.herow.sdk.connection

import android.content.Context
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
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var fakeAPI: FakeAPI
    private lateinit var dataHolder: DataHolder
    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        dataHolder = DataHolder(context)
        val serverURL = mockWebServer.url("/").toString()
        fakeAPI = RetrofitBuilder.buildRetrofitForAPI(context, serverURL, FakeAPI::class.java, true)
    }

    @Test
    fun testRequest() = runBlocking {
        mockWebServer.enqueue(MockResponse())
        fakeAPI.getAnswerToUniverse().execute()
        val headers = mockWebServer.takeRequest().headers.names()
        Assert.assertFalse(headers.contains(SessionInterceptor.AUTHORIZATION_HEADER))
        Assert.assertFalse(headers.contains(SessionInterceptor.DEVICE_ID_HEADER))
        Assert.assertFalse(headers.contains(SessionInterceptor.HEROW_ID_HEADER))

        dataHolder["connection.access_token"] = UUID.randomUUID().toString()
        dataHolder["connection.device_id"] = UUID.randomUUID().toString()
        dataHolder["connection.herow_id"] = UUID.randomUUID().toString()

        mockWebServer.enqueue(MockResponse())
        fakeAPI.getAnswerToUniverse().execute()
        val newHeaders = mockWebServer.takeRequest().headers.names()
        Assert.assertTrue(newHeaders.contains(SessionInterceptor.AUTHORIZATION_HEADER))
        Assert.assertTrue(newHeaders.contains(SessionInterceptor.DEVICE_ID_HEADER))
        Assert.assertTrue(newHeaders.contains(SessionInterceptor.HEROW_ID_HEADER))
    }
}