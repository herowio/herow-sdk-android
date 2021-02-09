package io.herow.sdk.connection.cache.dao

import androidx.room.*
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.CampaignWithCappingAndTriggerAndNotification

@Dao
interface CampaignDAO {

    @Insert
    fun insertCampaign(vararg campaign: Campaign)

    @Transaction
    @Query("SELECT * FROM Campaign")
    fun getAllCampaign(): List<CampaignWithCappingAndTriggerAndNotification>
}