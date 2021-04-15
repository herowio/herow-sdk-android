package io.herow.sdk.connection

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class)
class MockServerTest {

    private lateinit var result: Response<CacheResult>
    private lateinit var api: FakeAPI

    private lateinit var context: Context
    private lateinit var herowDatabase: HerowDatabase

    private lateinit var zoneDAO: ZoneDAO
    private lateinit var poiDAO: PoiDAO
    private lateinit var campaignDAO: CampaignDAO
    private lateinit var zoneRepository: ZoneRepository
    private lateinit var poiRepository: PoiRepository
    private lateinit var campaignRepository: CampaignRepository

    @Before
    fun setUp() {
        api = CompanySingleton.retrofit.create(FakeAPI::class.java)

        context = ApplicationProvider.getApplicationContext()
        herowDatabase = Room
            .inMemoryDatabaseBuilder(context, HerowDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        zoneDAO = herowDatabase.zoneDAO()
        zoneRepository = ZoneRepository(zoneDAO)

        poiDAO = herowDatabase.poiDAO()
        poiRepository = PoiRepository(poiDAO)

        campaignDAO = herowDatabase.campaignDAO()
        campaignRepository = CampaignRepository(campaignDAO)

        runBlocking {
            result = api.cache()
        }
    }

    @Test
    fun accessServer() {
        runBlocking {
            assertThat(result.isSuccessful, equalTo(true))
        }
    }

    @Test
    fun saveZoneIntoDBAndCheckResult() {
        runBlocking {
            if (result.body()?.zones != null) {
                for (zone in result.body()?.zones!!) {
                    zoneRepository.insert(zone)
                }

                val zonesInDB = zoneRepository.getAllZones()

                Assert.assertNotEquals(zonesInDB?.size, "0")
            }
        }
    }

    @Test
    fun savePOIIntoDBAndCheckResult() {
        runBlocking {
            if (result.body()?.pois != null) {
                for (poi in result.body()?.pois!!) {
                    poiRepository.insert(poi)
                }

                val poisInDB = poiRepository.getAllPois()

                Assert.assertNotEquals(poisInDB?.size, "0")
            }
        }
    }


    @Test
    fun saveCampaignAndCheckResult() {
        runBlocking {
            if (result.body()?.campaigns != null) {
                for (campaign in result.body()?.campaigns!!) {
                    campaignRepository.insert(campaign)
                }

                val campaignsInDB = campaignRepository.getAllCampaigns()
                Assert.assertNotEquals(campaignsInDB?.size, 0)

                herowDatabase.clearAllTables()

                val deletedCampaign = campaignRepository.getAllCampaigns()
                Assert.assertTrue(deletedCampaign.isNullOrEmpty())
            }
        }
    }

    @After
    fun close() {
        herowDatabase.close()
    }
}


object CompanySingleton {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val original = chain.request()

            val requestBuilder = original
                .newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "OAuth test")
                .addHeader("x-device-id", "test")
                .addHeader("x-herow-id", "test")
                .addHeader("x-sdk", "test")

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        ).build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://herow-sdk-backend-poc.ew.r.appspot.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
