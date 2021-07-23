package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone

interface ICampaignRepository {
    fun insert(campaign: Campaign)
    fun update(campaign: Campaign)
    fun getCampaignByID(id: String): Campaign?
    fun getAllCampaigns(): List<Campaign>?
    fun getAllCampaignsByZone(zone: Zone): List<Campaign>?
}