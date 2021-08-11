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
import kotlinx.coroutines.withContext
import org.junit.After
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
class ValidityFilterTest : AutoCloseKoinTest() {

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
    fun testValidityFilter() =
        runBlocking {
            var campaign: Campaign? = withContext(ioDispatcher) {
                MockDataInDatabase().createCampaignWithLateBegin(ioDispatcher)
            }

            // We have a Begin value after current time
            // We should not have a notification
            Assert.assertFalse(ValidityFilter.createNotification(campaign!!, sessionHolder))
            campaign = withContext(ioDispatcher) {
                MockDataInDatabase().createCampaignWithNoEnd(ioDispatcher)
            }

            // We have a Begin value before current time and we have no End value defined
            // We should have a notification
            Assert.assertTrue(ValidityFilter.createNotification(campaign, sessionHolder))
            campaign = withContext(ioDispatcher) {
                MockDataInDatabase().updateCampaignWithEndBefore(campaign as Campaign, ioDispatcher)
            }

            // We add an End value before current time
            // We should have a notification
            Assert.assertFalse(ValidityFilter.createNotification(campaign, sessionHolder))
            campaign = withContext(ioDispatcher) {
                MockDataInDatabase().updateCampaignWithEndAfter(campaign as Campaign, ioDispatcher)
            }

            // We update End and set it after current time
            Assert.assertTrue(ValidityFilter.createNotification(campaign, sessionHolder))
            campaign = withContext(ioDispatcher) {
                MockDataInDatabase().createAndInsertCampaignTwo(ioDispatcher)
            }

            // We add a campagne with no begin value
            // We should have a notification
            Assert.assertTrue(ValidityFilter.createNotification(campaign, sessionHolder))
        }

    @After
    fun cleanUp() {
        stopKoin()
    }
}