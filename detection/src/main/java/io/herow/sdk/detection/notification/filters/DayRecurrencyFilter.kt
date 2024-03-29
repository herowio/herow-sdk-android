package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.helpers.DateHelper

object DayRecurrencyFilter : INotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        var result = false

        val currentDay = DateHelper.getCurrentWeekDay()
        GlobalLogger.shared.info(null, "Current day is: $currentDay")

        val recurrencies = campaign.daysRecurrence?.map { it.uppercase() }

        if (recurrencies == null || recurrencies.isEmpty()) {
            return true
        }

        for (day in recurrencies) {
            result = day == currentDay
            if (result) {
                break
            }
        }

        val can = if (result) {
            "CAN"
        } else {
            "CAN NOT"
        }

        GlobalLogger.shared.info(
            null,
            "DayRecurrencyFilter: ${campaign.name} $can create notification"
        )

        GlobalLogger.shared.debug(
            null,
            "DayRecurrencyFilter will display: $result for campaign $campaign"
        )

        return result
    }
}