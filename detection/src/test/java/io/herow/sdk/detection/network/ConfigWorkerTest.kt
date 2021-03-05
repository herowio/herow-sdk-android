package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigListener
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ConfigWorkerTest {
    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: ConfigWorker
    private lateinit var herowAPI: HerowAPI

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)
        herowAPI = RetrofitBuilder.buildRetrofitForAPI(
            sessionHolder,
            HerowAPI.PRE_PROD_BASE_URL,
            HerowAPI::class.java
        )

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        worker = TestListenableWorkerBuilder<ConfigWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.PRE_PROD.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID
                )
            ).build()
    }


    @Test
    fun testLaunchTokenConfigWorker() {
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        }
    }

    @Test
    fun testLaunchUserInfoConfigWorker() {
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
        }
    }

    @Test
    fun testConfigResult() {
        val configWorkerListener = ConfigWorkerListener()
        ConfigDispatcher.addConfigListener(configWorkerListener)
        Assert.assertNull(configWorkerListener.configResult)

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertNotNull(configWorkerListener.configResult)
        }
    }

    @Test
    fun testWithCacheTimeSaved() {
        sessionHolder.saveModifiedCacheTime("Tue, 25 Aug 2020 12:57:38 GMT")

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(!sessionHolder.hasNoCacheTimeSaved())
        }
    }

    @Test
    fun testWithCacheTimeSuperiorToRemoteTime() {
        sessionHolder.saveModifiedCacheTime("Fri, 5 Mar 2021 12:57:38 GMT")

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertFalse(sessionHolder.getUpdateCacheStatus())
        }
    }

    @Test
    fun testWithCacheTimeLowerToRemoteTime() {
        sessionHolder.saveModifiedCacheTime("Mon, 24 Aug 2020 12:57:38 GMT")

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertTrue(sessionHolder.getUpdateCacheStatus())
        }
    }

    @Test
    fun userInfoShouldNotBeCalledIfUserIsSavedAndUpToDate() {
        val authRequest: AuthRequests = mock(AuthRequests::class.java)

        // Create same object that the saved one
        val userInfo = UserInfo(
            arrayListOf(Optin(value = false)),
            null,
            "randomCustom"
        )
        sessionHolder.saveStringUserInfo(Gson().toJson(userInfo))

        runBlocking {
            verify(authRequest, never()).launchUserInfoRequest(sessionHolder, herowAPI)
        }
    }
}

class ConfigWorkerListener(var configResult: ConfigResult? = null) : ConfigListener {
    override fun onConfigResult(configResult: ConfigResult) {
        this.configResult = configResult
    }
}