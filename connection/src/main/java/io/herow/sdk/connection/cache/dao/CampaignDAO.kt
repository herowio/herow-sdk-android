package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.herow.sdk.connection.cache.model.Campaign

@Dao
interface CampaignDAO {

    @Insert
    fun insertCampaign(campaign: Campaign): Long?

    @Transaction
    @Query("SELECT * FROM Campaign")
    fun getAllCampaign(): List<Campaign>?
}