package io.herow.sdk.connection.cache.dao

import androidx.room.*
import io.herow.sdk.connection.cache.model.Campaign

@Dao
interface CampaignDAO {

    @Insert
    fun insertCampaign(campaign: Campaign)

    @Update
    fun updateCampaign(campaign: Campaign)

    @Query("SELECT * FROM Campaign WHERE id = :id")
    fun getCampaignByID(id: String): Campaign?

    @Transaction
    @Query("SELECT * FROM Campaign")
    fun getAllCampaign(): List<Campaign>?

    @Transaction
    @Query("SELECT * FROM Campaign c WHERE c.id IN (SELECT z.campaigns From Zone z WHERE z.hash = :zoneHash)")
    fun getAllCampaignsByZoneHash(zoneHash: String): List<Campaign>?
}