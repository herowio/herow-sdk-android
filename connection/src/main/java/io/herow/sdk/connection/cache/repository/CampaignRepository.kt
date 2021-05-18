package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone

class CampaignRepository constructor(
    private val campaignDAO: CampaignDAO
) :
    ICampaignRepository {

    override fun insert(campaign: Campaign) = campaignDAO.insertCampaign(campaign)

    override fun update(campaign: Campaign) = campaignDAO.updateCampaign(campaign)

    override fun getCampaignByID(id: String): Campaign? = campaignDAO.getCampaignByID(id)

    override fun getAllCampaigns(): List<Campaign>? = campaignDAO.getAllCampaign()

    override fun getAllCampaignsByZone(zone: Zone): List<Campaign>? =
        campaignDAO.getAllCampaignsByZoneHash(zone.hash)
}