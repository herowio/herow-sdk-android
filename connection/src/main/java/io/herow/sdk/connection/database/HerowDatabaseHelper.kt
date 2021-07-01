package io.herow.sdk.connection.database

import android.content.Context
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object HerowDatabaseHelper {
    fun getPoiRepository(context: Context) =
        PoiRepository(HerowDatabase.getDatabase(context).poiDAO())

    fun getZoneRepository(context: Context) =
        ZoneRepository(HerowDatabase.getDatabase(context).zoneDAO())

    fun getCampaignRepository(context: Context) =
        CampaignRepository(HerowDatabase.getDatabase(context).campaignDAO())

    fun deleteDatabase(context: Context) = runBlocking {
        withContext(Dispatchers.IO) {
            HerowDatabase.getDatabase(context).clearAllTables()
        }
    }
}
