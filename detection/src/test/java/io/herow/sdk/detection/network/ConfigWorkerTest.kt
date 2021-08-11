package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.config.IConfigListener
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.detection.koin.databaseModuleTest
import io.herow.sdk.detection.koin.dispatcherModule
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ConfigWorkerTest: KoinTest {
    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: ConfigWorker
    private lateinit var herowAPI: IHerowAPI

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(databaseModuleTest, dispatcherModule)
        }

        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)
        sessionHolder.saveSDKID("test")
        sessionHolder.saveOptinValue(true)
        herowAPI = RetrofitBuilder.buildRetrofitForAPI(
            sessionHolder,
            IHerowAPI.TEST_BASE_URL,
            IHerowAPI::class.java
        )

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        worker = TestListenableWorkerBuilder<ConfigWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name,
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
        sessionHolder.saveModifiedCacheTime("Mon, 30 Jun 2025 12:57:38 GMT")

        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(ListenableWorker.Result.success()))
            Assert.assertFalse(sessionHolder.getUpdateCacheStatus())
        }
    }

    @Test
    fun testWithCacheTimeLowerToRemoteTime() {
        sessionHolder.saveModifiedCacheTime("Tue, 9 Jun 2020 12:57:38 GMT")

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

    @After
    fun cleanUp() {
        stopKoin()
    }

}

class ConfigWorkerListener(var configResult: ConfigResult? = null) : IConfigListener {
    override fun onConfigResult(configResult: ConfigResult) {
        this.configResult = configResult
    }
}