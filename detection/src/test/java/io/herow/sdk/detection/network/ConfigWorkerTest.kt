package io.herow.sdk.detection.network

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.config.ConfigDispatcher
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.config.IConfigListener
import io.herow.sdk.connection.userinfo.*
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import io.herow.sdk.detection.session.RetrofitBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.test.KoinTest
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ConfigWorkerTest : KoinTest, ICustomKoinTestComponent {
    private val ioDispatcher: CoroutineDispatcher by inject()
    private val sessionHolder: SessionHolder by inject()

    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var worker: ConfigWorker
    private lateinit var herowAPI: IHerowAPI

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()

        sessionHolder.saveSDKID("test")
        sessionHolder.saveOptinValue(true)

        herowAPI = RetrofitBuilder.buildRetrofitForAPI(
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
        worker.testing = true
    }


    @Test
    fun testLaunchTokenConfigWorker() {
        runBlocking {
            withContext(ioDispatcher) {
                val result = worker.doWork()
                assertThat(result, `is`(ListenableWorker.Result.success()))
                Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
                Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())
            }
        }
    }

    @Test
    fun testConfigResult() {
        val configWorkerListener = ConfigWorkerListener()
        ConfigDispatcher.addConfigListener(configWorkerListener)
        Assert.assertNull(configWorkerListener.configResult)

        runBlocking {
            withContext(ioDispatcher) {
                val result = worker.doWork()
                assertThat(result, `is`(ListenableWorker.Result.success()))
                Assert.assertNotNull(configWorkerListener.configResult)
            }
        }

        ConfigDispatcher.unregisterConfigListener(configWorkerListener)
    }

    @Test
    fun testWithCacheTimeSaved() {
        sessionHolder.saveModifiedCacheTime("Tue, 25 Aug 2020 12:57:38 GMT")

        runBlocking {
            withContext(ioDispatcher) {
                val result = worker.doWork()
                assertThat(result, `is`(ListenableWorker.Result.success()))
                Assert.assertTrue(!sessionHolder.hasNoCacheTimeSaved())
            }
        }
    }

    @Test
    fun testWithCacheTimeSuperiorToRemoteTime() {
        sessionHolder.saveModifiedCacheTime("Mon, 30 Jun 2025 12:57:38 GMT")

        runBlocking {
            withContext(ioDispatcher) {
                val result = worker.doWork()
                assertThat(result, `is`(ListenableWorker.Result.success()))
                Assert.assertFalse(sessionHolder.getUpdateCacheStatus())
            }
        }
    }

    @Test
    fun testWithCacheTimeLowerToRemoteTime() {
        sessionHolder.saveModifiedCacheTime("Tue, 9 Jun 2020 12:57:38 GMT")

        runBlocking {
            withContext(ioDispatcher) {
                val result = worker.doWork()
                assertThat(result, `is`(ListenableWorker.Result.success()))
                Assert.assertTrue(sessionHolder.getUpdateCacheStatus())
            }
        }
    }

    @Test
    fun userInfoShouldNotBeCalledIfUserIsSavedAndUpToDate() {
        val authRequest: AuthRequests = mock(AuthRequests::class.java)
        val permissionLocation = PermissionLocation(
            Precision.FINE,
            Status.ALWAYS
        )

        // Create same object that the saved one
        val userInfo = UserInfo(
            arrayListOf(Optin(value = false)),
            null,
            "randomCustom",
            permissionLocation
        )
        sessionHolder.saveStringUserInfo(Gson().toJson(userInfo))

        runBlocking {
            verify(authRequest, never()).launchUserInfoRequest(herowAPI)
        }
    }
}

class ConfigWorkerListener(var configResult: ConfigResult? = null) : IConfigListener {
    override fun onConfigResult(configResult: ConfigResult) {
        this.configResult = configResult
    }
}