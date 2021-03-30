package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.model.Campaign

class CampaignRepository constructor(
    private val campaignDAO: CampaignDAO
) :
    ICampaignRepository {

    override fun insert(campaign: Campaign) {
        campaignDAO.insertCampaign(campaign)
    }

    override fun getAllCampaigns(): List<Campaign>? = campaignDAO.getAllCampaign()
}