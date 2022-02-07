package io.herow.sdk.detection.network

import android.content.Context
import android.os.Looper.getMainLooper
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.base.Verify.verify
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowAPITest: KoinTest, ICustomKoinTestComponent {
    private val sessionHolder: SessionHolder by inject()

    private lateinit var herowInitializer: HerowInitializer
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()

        herowInitializer = HerowInitializer.getInstance(context, true)

        sessionHolder.saveOptinValue(true)
        sessionHolder.saveSDKID("test")
        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
    }

    @After
    fun cleanUp() {
        herowInitializer.reset()
    }

    @Test
    fun testWorkerWithPreProdURL() {
        herowInitializer
            .configApp(NetworkConstants.USERNAME, NetworkConstants.PASSWORD)
            .configPlatform(HerowPlatform.PRE_PROD)
            .synchronize()

        verify(sessionHolder.getCustomPreProdURL() == Constants.DEFAULT_PRE_PROD_URL)

        herowInitializer.setPreProdCustomURL("https://chat.chien.io")
        Assert.assertFalse(sessionHolder.getCustomPreProdURL() == Constants.DEFAULT_PRE_PROD_URL)
        Assert.assertTrue(sessionHolder.getCustomPreProdURL() == "https://chat.chien.io")
    }

    @Test
    fun testWorkerWithProdURL() {
        herowInitializer
            .configApp(NetworkConstants.USERNAME, NetworkConstants.PASSWORD)
            .configPlatform(HerowPlatform.PROD)
            .synchronize()

        verify(sessionHolder.getCustomProdURL() == Constants.DEFAULT_PROD_URL)

        herowInitializer.setProdCustomURL("https://poule.canard.io")
        shadowOf(getMainLooper()).idle()

        verify(sessionHolder.getCustomProdURL() != Constants.DEFAULT_PROD_URL)
        verify(sessionHolder.getCustomProdURL() == "https://poule.canard.io")
    }
}