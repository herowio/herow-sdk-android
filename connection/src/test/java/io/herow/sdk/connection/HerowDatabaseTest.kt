package io.herow.sdk.connection

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.Access
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class HerowDatabaseTest {

    private lateinit var zoneDAO: ZoneDAO
    private lateinit var campaignDAO: CampaignDAO
    private lateinit var poiDAO: PoiDAO

    private lateinit var zoneRepository: ZoneRepository
    private lateinit var campaignRepository: CampaignRepository
    private lateinit var poiRepository: PoiRepository

    private lateinit var db: HerowDatabase

    private lateinit var zone: Zone
    private lateinit var campaign: Campaign
    private lateinit var poi: Poi

    private lateinit var context: Context

    @Before
    fun createDB() {
        context = ApplicationProvider.getApplicationContext()
        db = Room
            .inMemoryDatabaseBuilder(context, HerowDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        zoneDAO = db.zoneDAO()
        campaignDAO = db.campaignDAO()
        poiDAO = db.poiDAO()

        zoneRepository = ZoneRepository(zoneDAO)
        campaignRepository = CampaignRepository(campaignDAO)
        poiRepository = PoiRepository(poiDAO)
    }

    @Before
    fun populateDB() {
        zone = Zone(
            hash = "testHashZone",
            lat = 48.11198,
            lng = -1.67429,
            radius = 1.0,
            access = Access("accessID", "Home", "Random address, 75000 Paris"),
            campaigns = null
        )

        runBlocking {
            zoneRepository.insert(zone)
        }

        campaign = Campaign(
            id = "campaignID1",
            name = "testCampaign1",
            begin = TimeHelper.convertLocalDateTimeToTimestamp(
                java.time.LocalDateTime.of(
                    2021,
                    1,
                    30,
                    8,
                    0,
                    0,
                    0
                )
            ),
            capping = null,
            notification = null
        )

        runBlocking {
            campaignRepository.insert(campaign)
        }

        poi = Poi(
            id = "testIDPoi",
            lat = 12.0,
            lng = 13.0,
            tags = listOf("Tag1", "Tag2")
        )

        runBlocking {
            poiRepository.insert(poi)
        }

    }

    @Test
    @Throws(Exception::class)
    fun createZoneAndReadInList() {
        runBlocking {
            val listOfZone = zoneRepository.getAllZones()
            assertThat(listOfZone?.get(0)?.hash, equalTo(zone.hash))
        }

    }

    @Test
    @Throws(Exception::class)
    fun campaignTableShouldNotBeEmpty() {
        runBlocking {
            val listOfCampaigns = campaignRepository.getAllCampaigns()
            assertThat(listOfCampaigns?.size, equalTo(1))
        }

    }

    @Test
    @Throws(Exception::class)
    fun poiTableShouldNotBeEmpty() {
        runBlocking {
            val listOfPois = poiRepository.getAllPois()
            assertThat(listOfPois?.size, equalTo(1))
        }
    }

    @Test
    @Throws(Exception::class)
    fun tagToPoiShouldMatch() {
        runBlocking {
            val listOfPois = poiRepository.getAllPois()

            val expected = "Tag1"

            assertThat(listOfPois?.first()?.tags?.get(0), equalTo(expected))
        }
    }

    @Test
    @Throws(Exception::class)
    fun addCampaignToZone() {
        runBlocking {
            val listOfCampaigns = campaignRepository.getAllCampaigns()

            Assert.assertNotNull(listOfCampaigns)
            assertThat(listOfCampaigns?.get(0)?.name, equalTo("testCampaign1"))
        }
    }

    @Test
    @Throws(Exception::class)
    fun checkAccessOfZone() {
        assertThat(zone.access!!.name, equalTo("Home"))
    }

    /* @After
    @Throws(IOException::class)
    fun closeDB() {
        db.close()
    } */
}