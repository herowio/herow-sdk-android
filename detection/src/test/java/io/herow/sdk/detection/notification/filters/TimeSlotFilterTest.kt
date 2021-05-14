package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
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
class TimeSlotFilterTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
    }

    @Test
    fun testTimeSlotFilter() {
        var campaign: Campaign?

        runBlocking {
            campaign = MockDataInDatabase(context).createAndInsertCampaignTwo()
        }

        // We have no startHour and no stopHour
        // We should have a notification
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithOnlyStartHour()
        }

        // We have a value for startHour but no value for stopHour
        // We should have a notification since the platform send back a default value
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithOnlyStopHour()
        }

        // We have no value for startHour but a value for stopHour
        // We should have a notification since the platform send back a default value
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithShortSlot()
        }

        // We have a value for startHour and for stopHour
        // We only have a gap of 1 hour early in the morning
        // We should not have a notification because this test is done in daytime
        Assert.assertFalse(TimeSlotFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithLongSlot()
        }

        // We have a value for startHour and for stopHour
        // We only have a long gap between the two value
        // We should have a notification
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign!!, sessionHolder))
    }
}