package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Campaign

interface ICampaignRepository {

    fun insert(campaign: Campaign)
    fun getAllCampaigns(): List<Campaign>?
}