package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign

object ValidityFilter : NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        val now = TimeHelper.getCurrentTime()

        val start: Long? = campaign.begin
        val end: Long? = campaign.end

        if (start == null || start == 0.toLong()) {
            GlobalLogger.shared.info(null, "Start in ValidityFilter is not usable")
            return true
        }

        var result = start < now

        if (result && (end != null && end != 0.toLong())) {
            result = end > now
        }

        GlobalLogger.shared.info(null, "Validity filter result is: $result")
        return result
    }
}