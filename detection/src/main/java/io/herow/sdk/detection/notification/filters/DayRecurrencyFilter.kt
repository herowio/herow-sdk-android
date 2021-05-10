package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.helpers.DateHelper
import java.util.*

object DayRecurrencyFilter : NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        var result = false

        val currentDay = DateHelper.getCurrentWeekDay()
        GlobalLogger.shared.info(null, "Current day is: $currentDay")

        val recurrencies = campaign.daysRecurrence?.map { it.toUpperCase(Locale.ROOT) }

        if (recurrencies?.count() == 0) {
            return true
        }

        if (recurrencies != null) {
            for (day in recurrencies) {
                result = day == currentDay
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
        return true
    }
}