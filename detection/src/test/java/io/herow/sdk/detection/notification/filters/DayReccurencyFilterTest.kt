package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.HerowInitializer
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.helpers.DateHelper
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

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DayReccurencyFilterTest : KoinTest, ICustomKoinTestComponent {

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
    fun testDayReccurency(): Unit = runBlocking {
        withContext(ioDispatcher) {
            if (DateHelper.getCurrentWeekDay() == "Wednesday") {
                var campaign: Campaign?

                campaign = MockDataInDatabase().createCampaignWithMondayTuesdayFriday()

                // This test has been done on Wednesday 12 of May
                // We should not have a notification
                Assert.assertFalse(
                    DayRecurrencyFilter.createNotification(
                        campaign,
                        sessionHolder
                    )
                )

                campaign = MockDataInDatabase().createCampaignWithWednesday()

                // Since we are Wednesday
                // We should have a notification
                Assert.assertTrue(DayRecurrencyFilter.createNotification(campaign, sessionHolder))

                campaign = MockDataInDatabase().createCampaignWithNoEnd()

                // We don't define any day into this campaign
                // We should have a notification
                Assert.assertTrue(DayRecurrencyFilter.createNotification(campaign, sessionHolder))
            }
        }
    }
}