package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.helpers.DateHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DayReccurencyFilterTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
    }

    @Test
    fun testDayReccurency() {
        if (DateHelper.getCurrentWeekDay() == "Wednesday") {
            var campaign: Campaign?

            runBlocking {
                campaign = MockDataInDatabase(context).createCampaignWithMondayTuesdayFriday()
            }

            // This test has been done on Wednesday 12 of May
            // We should not have a notification
            Assert.assertFalse(DayRecurrencyFilter.createNotification(campaign!!, sessionHolder))

            runBlocking {
                campaign = MockDataInDatabase(context).createCampaignWithWednesday()
            }

            // Since we are Wednesday
            // We should have a notification
            Assert.assertTrue(DayRecurrencyFilter.createNotification(campaign!!, sessionHolder))

            runBlocking {
                campaign = MockDataInDatabase(context).createCampaignWithNoEnd()
            }

            // We don't define any day into this campaign
            // We should have a notification
            Assert.assertTrue(DayRecurrencyFilter.createNotification(campaign!!, sessionHolder))
        }
    }
}