package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.koin.databaseModuleTest
import io.herow.sdk.detection.koin.dispatcherModule
import io.herow.sdk.detection.helpers.DateHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class DayReccurencyFilterTest : AutoCloseKoinTest() {

    private val ioDispatcher: CoroutineDispatcher by inject()
    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder

    @Before
    fun setUp() {
        stopKoin()
        startKoin {
            allowOverride(true)
            androidContext(ApplicationProvider.getApplicationContext())
            modules(databaseModuleTest, dispatcherModule)
        }

        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionHolder = SessionHolder(DataHolder(context))
    }

    @Test
    fun testDayReccurency() = runBlocking {
        if (DateHelper.getCurrentWeekDay() == "Wednesday") {
            var campaign: Campaign?

            campaign =
                MockDataInDatabase().createCampaignWithMondayTuesdayFriday(ioDispatcher)

            // This test has been done on Wednesday 12 of May
            // We should not have a notification
            Assert.assertFalse(
                DayRecurrencyFilter.createNotification(
                    campaign,
                    sessionHolder
                )
            )

            campaign =
                MockDataInDatabase().createCampaignWithWednesday(ioDispatcher)

            // Since we are Wednesday
            // We should have a notification
            Assert.assertTrue(DayRecurrencyFilter.createNotification(campaign, sessionHolder))

            campaign =
                MockDataInDatabase().createCampaignWithNoEnd(ioDispatcher)

            // We don't define any day into this campaign
            // We should have a notification
            Assert.assertTrue(DayRecurrencyFilter.createNotification(campaign, sessionHolder))
        }
    }
}