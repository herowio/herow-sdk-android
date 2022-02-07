package io.herow.sdk.detection.analytics

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowLogContextTest: ICustomKoinTestComponent {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sessionHolder: SessionHolder by inject()

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)

        sessionHolder.reset()
    }

    @Test
    fun testHerowLogContextIsParcelable() {
        val location = Location("tus")
        location.latitude = 42.6
        location.longitude = 2.5

        //TODO Complete logContext
        val herowLogContext = HerowLogContext("fg", location)
        herowLogContext.enrich(ApplicationData(ApplicationProvider.getApplicationContext()), sessionHolder)
        val listOfLogs = listOf(Log(herowLogContext))
        val logs = Logs(listOfLogs)
        val logJsonString = GsonProvider.toJson(logs)
        Assert.assertNotNull(logJsonString)
    }
}