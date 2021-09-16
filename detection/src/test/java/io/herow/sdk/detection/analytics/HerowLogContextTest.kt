package io.herow.sdk.detection.analytics

import android.location.Location
import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.analytics.model.HerowLogContext
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowLogContextTest {
    private val sessionHolder = SessionHolder(DataHolder(ApplicationProvider.getApplicationContext()))

    @Test
    fun testHerowLogContextIsParcelable() {
        val location = Location("tus")
        location.latitude = 42.6
        location.longitude = 2.5

        val herowLogContext = HerowLogContext(sessionHolder, "fg", location)
        herowLogContext.enrich(
            ApplicationData(ApplicationProvider.getApplicationContext()), SessionHolder(
                DataHolder(ApplicationProvider.getApplicationContext())
            )
        )
        val listOfLogs = listOf(Log(herowLogContext))
        val logs = Logs(listOfLogs)
        val logJsonString = GsonProvider.toJson(logs)
        Assert.assertNotNull(logJsonString)
    }
}