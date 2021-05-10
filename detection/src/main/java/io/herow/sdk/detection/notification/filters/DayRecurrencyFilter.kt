package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.helpers.DateHelper
import java.util.*

object DayRecurrencyFilter : NotificationFilter {

    override fun createNotification(campaign: Campaign): Boolean {
        var result = false

        val currentDay = DateHelper.getCurrentWeekDay()
        GlobalLogger.shared.info(null, "Current day is: $currentDay")

        val recurrencies = campaign.daysRecurrence?.map { it.toUpperCase(Locale.ROOT) }

        if (recurrencies == null || recurrencies.count() == 0) {
            return true
        }

        for (day in recurrencies) {
            result = day == currentDay
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
        return true
    }
}