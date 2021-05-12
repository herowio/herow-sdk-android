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
            campaign = MockDataInDatabase(context).createCampaignWithLateBegin()
        }

        // We have a Begin value after current time
        // We should not have a notification
        Assert.assertFalse(ValidityFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithNoEnd()
        }

        // We have a Begin value before current time and we have no End value defined
        // We should have a notification
        Assert.assertTrue(ValidityFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).updateCampaignWithEndBefore(campaign as Campaign)
        }

        // We add an End value before current time
        // We should have a notification
        Assert.assertFalse(ValidityFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).updateCampaignWithEndAfter(campaign as Campaign)
        }

        // We update End and set it after current time
        Assert.assertTrue(ValidityFilter.createNotification(campaign!!, sessionHolder))

        runBlocking {
            campaign = MockDataInDatabase(context).createAndInsertCampaignTwo()
        }

        // We add a campagne with no begin value
        // We should have a notification
        Assert.assertTrue(ValidityFilter.createNotification(campaign!!, sessionHolder))
    }
}