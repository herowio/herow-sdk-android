package io.herow.sdk.connection

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.helpers.DateHelper
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Interval
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import java.io.IOException

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowDatabaseTest {

    private lateinit var zoneDAO: ZoneDAO
    private lateinit var campaignDAO: CampaignDAO

    private lateinit var zoneRepository: ZoneRepository
    private lateinit var campaignRepository: CampaignRepository

    private lateinit var db: HerowDatabase

    private lateinit var zone: Zone
    private lateinit var campaign: Campaign
    private lateinit var interval: Interval

    private lateinit var context: Context

    @Before
    fun createDB() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, HerowDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        zoneDAO = db.zoneDAO()
        campaignDAO = db.campaignDAO()

        zoneRepository = ZoneRepository(zoneDAO)
        campaignRepository = CampaignRepository(campaignDAO)
    }

    @Before
    fun populateDB() {
        zone = Zone(
            hash = "testHashZone",
            lat = 48.11198,
            lng = -1.67429,
            radius = 1,
            access = Access("accessID", "Home", "Random address, 75000 Paris"),
            campaigns = null
        )

        zoneRepository.insert(zone)

        interval = Interval(
            start = TimeHelper.getCurrentTime() - 20000,
            end = TimeHelper.getCurrentTime()
        )

        val interval2 = Interval(
            start = TimeHelper.getCurrentTime() - 40000,
            end = TimeHelper.getCurrentTime() - 30000
        )

        campaign = Campaign(
            id = "campaignID1",
            company = "testCompany",
            createdDate = DateHelper.convertDateToMilliSeconds(
                LocalDateTime.of(
                    2020,
                    Month.JANUARY,
                    11,
                    11,
                    50,
                    0,
                    0
                ),
                context
            ),
            modifiedDate = DateHelper.convertDateToMilliSeconds(
                LocalDateTime.of(
                    2020,
                    Month.JANUARY,
                    12,
                    7,
                    0,
                    0,
                    0
                ),
                context
            ),
            deleted = false,
            simpleID = "testSimpleID",
            name = "testCampaign1",
            begin = DateHelper.convertDateToMilliSeconds(
                LocalDateTime.of(
                    2021,
                    Month.JANUARY,
                    30,
                    8,
                    0,
                    0,
                    0
                ),
                context
            ),
            recurrenceEnabled = false,
            timeZone = "Europe/Paris",
            capping = null,
            trigger = null,
            notification = null,
            intervals = listOf(interval, interval2)
        )

        campaignRepository.insert(campaign)
    }

    @Test
    @Throws(Exception::class)
    fun createZoneAndReadInList() {
        val listOfZone = zoneRepository.getAllZones()
        assertThat(listOfZone?.get(0)?.hash, equalTo(zone.hash))
    }

    @Test
    @Throws(Exception::class)
    fun campaignTableShouldNotBeEmpty() {
        val listOfCampaigns = campaignRepository.getAllCampaigns()
        assertThat(listOfCampaigns?.size, equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun addCampaignToZone() {
        val listOfCampaigns = campaignRepository.getAllCampaigns()

        Assert.assertNotNull(listOfCampaigns)
        assertThat(listOfCampaigns?.get(0)?.name, equalTo("testCampaign1"))
    }

    @Test
    @Throws(Exception::class)
    fun checkAccessOfZone() {
        assertThat(zone.access!!.name, equalTo("Home"))
    }

    @After
    @Throws(IOException::class)
    fun closeDB() {
        db.close()
    }
}