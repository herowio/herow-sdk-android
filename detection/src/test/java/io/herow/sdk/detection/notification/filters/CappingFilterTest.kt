package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.HerowCapping
import io.herow.sdk.detection.MockDataInDatabase
import io.herow.sdk.detection.databaseModuleTest
import io.herow.sdk.detection.dispatcherModule
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
import java.time.LocalDateTime

//TODO Check this one! - HerowCapping is null
@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class CappingFilterTest : AutoCloseKoinTest() {

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
    fun testCappingFilter() = runBlocking {
            var campaign: Campaign? = MockDataInDatabase().createAndInsertCampaignTwo(ioDispatcher)

            // We have no capping
            // We should have a notification
            Assert.assertTrue(CappingFilter.createNotification(campaign!!, sessionHolder))

            val herowCappingSaved: HerowCapping =
                sessionHolder.getHerowCapping(campaign)

            campaign = MockDataInDatabase().updateCampaignTwoWithCapping(ioDispatcher)

            herowCappingSaved.count = 7
            herowCappingSaved.razDate =
                TimeHelper.convertLocalDateTimeToTimestamp(LocalDateTime.of(2021, 7, 20, 0, 0, 0))
            sessionHolder.saveHerowCapping(
                campaign.id!!,
                GsonProvider.toJson(herowCappingSaved, HerowCapping::class.java)
            )

            // We create a Campaign with a Capping - MaxNumberOfNotifications is 5
            // HerowCapping count is 7 - Saved razTime is superior to current time
            // We should not have a notification
            Assert.assertFalse(CappingFilter.createNotification(campaign, sessionHolder))

            herowCappingSaved.razDate =
                TimeHelper.convertLocalDateTimeToTimestamp(LocalDateTime.of(2021, 4, 3, 12, 0, 0))
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