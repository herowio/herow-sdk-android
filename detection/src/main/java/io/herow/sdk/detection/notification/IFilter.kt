package io.herow.sdk.detection.notification

import io.herow.sdk.connection.cache.model.Campaign

interface IFilter {
    fun createNotificationForCampagn(campaign: Campaign): Boolean
}