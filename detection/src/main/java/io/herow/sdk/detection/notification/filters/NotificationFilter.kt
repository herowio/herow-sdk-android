package io.herow.sdk.detection.notification.filters

import io.herow.sdk.connection.cache.model.Campaign

interface NotificationFilter {

    fun createNotification(campaign: Campaign): Boolean
}