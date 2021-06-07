package io.herow.sdk.connection.database

import android.content.Context
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository

object HerowDatabaseHelper {
    fun getPoiRepository(context: Context) = PoiRepository(HerowDatabase.getDatabase(context).poiDAO())
    fun getZoneRepository(context: Context) = ZoneRepository(HerowDatabase.getDatabase(context).zoneDAO())
    fun getCampaignRepository(context: Context) = CampaignRepository(HerowDatabase.getDatabase(context).campaignDAO())
}