package io.herow.sdk.detection.notification.filters

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
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

@ExperimentalCoroutinesApi
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class ValidityFilterTest : KoinTest, ICustomKoinTestComponent {

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
    fun testValidityFilter(): Unit = runBlocking {
        withContext(ioDispatcher) {
            var campaign: Campaign? = MockDataInDatabase().createCampaignWithLateBegin()

            // We have a Begin value after current time
            // We should not have a notification
            Assert.assertFalse(ValidityFilter.createNotification(campaign!!, sessionHolder))
            campaign = MockDataInDatabase().createCampaignWithNoEnd()

            // We have a Begin value before current time and we have no End value defined
            // We should have a notification
            Assert.assertTrue(ValidityFilter.createNotification(campaign, sessionHolder))
            campaign = MockDataInDatabase().updateCampaignWithEndBefore(campaign)

            // We add an End value before current time
            // We should have a notification
            Assert.assertFalse(ValidityFilter.createNotification(campaign, sessionHolder))
            campaign = MockDataInDatabase().updateCampaignWithEndAfter(campaign)

            // We update End and set it after current time
            Assert.assertTrue(ValidityFilter.createNotification(campaign, sessionHolder))
            campaign = MockDataInDatabase().createAndInsertCampaignTwo()

            // We add a campagne with no begin value
            // We should have a notification
            Assert.assertTrue(ValidityFilter.createNotification(campaign, sessionHolder))
        }
    }
}