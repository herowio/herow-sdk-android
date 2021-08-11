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
class TimeSlotFilterTest : AutoCloseKoinTest() {

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private val ioDispatcher: CoroutineDispatcher by inject()

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
    fun testTimeSlotFilter() = runBlocking {
        var campaign: Campaign? =
            MockDataInDatabase().createAndInsertCampaignTwo(ioDispatcher)

        // We have no startHour and no stopHour
        // We should have a notification
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign!!, sessionHolder))

        campaign = MockDataInDatabase().createCampaignWithOnlyStartHour(ioDispatcher)

        // We have a value for startHour but no value for stopHour
        // We should have a notification since the platform send back a default value
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign, sessionHolder))

        campaign = MockDataInDatabase().createCampaignWithOnlyStopHour(ioDispatcher)

        // We have no value for startHour but a value for stopHour
        // We should have a notification since the platform send back a default value
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign, sessionHolder))

        campaign = MockDataInDatabase().createCampaignWithShortSlot(ioDispatcher)

        // We have a value for startHour and for stopHour
        // We only have a gap of 1 hour early in the morning
        // We should not have a notification because this test is done in daytime
        Assert.assertFalse(TimeSlotFilter.createNotification(campaign, sessionHolder))

        campaign = MockDataInDatabase().createCampaignWithLongSlot(ioDispatcher)

        // We have a value for startHour and for stopHour
        // We only have a long gap between the two value
        // We should have a notification
        Assert.assertTrue(TimeSlotFilter.createNotification(campaign, sessionHolder))
    }
}