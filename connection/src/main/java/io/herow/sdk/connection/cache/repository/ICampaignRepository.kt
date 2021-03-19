package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Campaign

interface ICampaignRepository {

    suspend fun insert(campaign: Campaign)
    suspend fun getAllCampaigns(): List<Campaign>?
}