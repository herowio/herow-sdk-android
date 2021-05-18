package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.HerowCapping
import io.herow.sdk.detection.MockDataInDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CappingFilterTest {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
    }

    @Test
    fun testCappingFilter() {
        var campaign: Campaign?

        runBlocking {
            campaign = MockDataInDatabase(context).createAndInsertCampaignTwo()
        }
        val herowCappingNotSaved: HerowCapping = HerowCapping().convertMapperToCapping(sessionHolder.getHerowCapping())

        // We have no capping
        // We should have a notification
        Assert.assertTrue(CappingFilter.createNotification(campaign!!, sessionHolder))

        val herowCappingSaved: HerowCapping = HerowCapping().convertMapperToCapping(sessionHolder.getHerowCapping())

        runBlocking {
            campaign = MockDataInDatabase(context).createCampaignWithCapping()
        }

        herowCappingSaved.count = 7
        herowCappingSaved.razDate = LocalDateTime.of(2021, 7, 20, 3, 3, 30)
        sessionHolder.saveHerowCapping(GsonProvider.toJson(herowCappingSaved, HerowCapping::class.java))

        // We create a Campaign with a Capping - MaxNumberOfNotifications is 5
        // HerowCapping count is 7 - Saved razTime is superior to current time
        // We should not have a notification
        Assert.assertFalse(CappingFilter.createNotification(campaign!!, sessionHolder))

        herowCappingSaved.razDate = LocalDateTime.of(2021, 4, 3, 12, 0, 0)
        sessionHolder.saveHerowCapping(GsonProvider.toJson(herowCappingSaved, HerowCapping::class.java))

        // With the same campaign, the saved razTime is inferior to currentTime
        // We should have a notification
        Assert.assertTrue(CappingFilter.createNotification(campaign!!, sessionHolder))

        sessionHolder.saveHerowCapping("")

        CappingFilter.createNotification(campaign!!, sessionHolder)

        // HerowCapping's campaignID should be the same as the campaign id given as parameter
        Assert.assertTrue(campaign!!.id == sessionHolder.getHerowCapping().campaignId)
    }
}