package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.MockDataInDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ValidityFilterTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
    }

    @Test
    fun testValidityFilter() {
        var campaign: Campaign?

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithNoEnd()
        }

        println("Campaign is $campaign")
        Assert.assertTrue(ValidityFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithLateBegin()
        }

        println("Campaign is $campaign")
        println("Now is: ${TimeHelper.getCurrentTime()}")
        Assert.assertFalse(ValidityFilter.createNotification(campaign!!, sessionHolder))
    }
}