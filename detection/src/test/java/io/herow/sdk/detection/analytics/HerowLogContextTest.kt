package io.herow.sdk.detection.analytics

import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.google.gson.GsonBuilder
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.logs.Log
import io.herow.sdk.connection.logs.Logs
import io.herow.sdk.detection.analytics.model.HerowLogContext
import io.herow.sdk.detection.analytics.adapter.LocationAdapter
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowLogContextTest {
    @Test
    fun testHerowLogContextIsParcelable() {
        val location = Location("tus")
        location.latitude = 42.6
        location.longitude = 2.5

        val herowLogContext = HerowLogContext("fg", location)
        herowLogContext.enrich(ApplicationData(ApplicationProvider.getApplicationContext()), SessionHolder(
            DataHolder(ApplicationProvider.getApplicationContext())
        ))
        val listOfLogs = listOf(Log(herowLogContext, TimeHelper.getCurrentTime()))
        val gson = GsonBuilder().registerTypeAdapter(Location::class.java, LocationAdapter()).create()
        val logs = Logs(listOfLogs)
        val logJsonString = gson.toJson(logs)
        Assert.assertNotNull(logJsonString)
    }
}