package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.HerowCapping
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.koin.HerowKoinTestContext
import io.herow.sdk.detection.koin.ICustomKoinTestComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CappingFilterTest: KoinTest, ICustomKoinTestComponent {

    private val ioDispatcher: CoroutineDispatcher by inject()
    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val sessionHolder: SessionHolder by inject()

    @Before
    fun setUp() {
        HerowInitializer.setStaticTesting(true)
        HerowKoinTestContext.init(context)
        sessionHolder.reset()
    }

    @Test
    fun testCappingFilter(): Unit = runBlocking {
        withContext(ioDispatcher) {
            var campaign: Campaign? = MockDataInDatabase().createAndInsertCampaignTwo()

            // We have no capping
            // We should have a notification
            Assert.assertTrue(CappingFilter.createNotification(campaign!!, sessionHolder))

            val herowCappingSaved: HerowCapping =
                sessionHolder.getHerowCapping(campaign)

            campaign = MockDataInDatabase().updateCampaignTwoWithCapping()

            herowCappingSaved.count = 7
            herowCappingSaved.razDate =
                TimeHelper.convertLocalDateTimeToTimestamp(LocalDateTime.of(2024, 7, 20, 0, 0, 0))
            sessionHolder.saveHerowCapping(
                campaign.id!!,
                GsonProvider.toJson(herowCappingSaved, HerowCapping::class.java)
            )

            println("HerowCapping saved is: $herowCappingSaved")

            // We create a Campaign with a Capping - MaxNumberOfNotifications is 5
            // HerowCapping count is 7 - Saved razTime is superior to current time
            // We should not have a notification
            Assert.assertFalse(CappingFilter.createNotification(campaign, sessionHolder))

            herowCappingSaved.razDate =
                TimeHelper.convertLocalDateTimeToTimestamp(LocalDateTime.of(2020, 4, 3, 12, 0, 0))
            sessionHolder.saveHerowCapping(
                campaign.id!!,
                GsonProvider.toJson(herowCappingSaved, HerowCapping::class.java)
            )

            // With the same campaign, the saved razTime is inferior to currentTime
            // We should have a notification
            Assert.assertTrue(CappingFilter.createNotification(campaign, sessionHolder))

            sessionHolder.removeSavedHerowCapping()
            CappingFilter.createNotification(campaign, sessionHolder)

            // HerowCapping's campaignID should be the same as the campaign id given as parameter
            Assert.assertTrue(campaign.id == sessionHolder.getHerowCapping(campaign).campaignId)
        }
    }
}