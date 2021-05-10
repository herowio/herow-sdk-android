package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign

object ValidityFilter: NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        val now = TimeHelper.getCurrentTime()
        var result = true

        val start: Long? = campaign.begin
        val end: Long? = campaign.end

        if (start != null) {
            if (start > now) {
                result = false
            }
        }

        if (end == 0.toLong()) {
            return true
        }

        if (end != null) {
            if (end < now) {
                result = false
            }
        }

        val can = if (result) {
            "CAN"
        } else {
            "CAN NOT"
        }

        GlobalLogger.shared.info(null, "ValidityFilter: ${campaign.name} $can create notification")
        return result
    }
}