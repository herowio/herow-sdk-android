package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import java.time.LocalTime

object TimeSlotFilter: NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        val now = TimeHelper.getCurrentLocalTime()

        if (campaign.startHour != null && campaign.stopHour != null) {
            val startComponent = campaign.startHour!!.split(":")
            val stopComponent = campaign.stopHour!!.split(":")

            if (startComponent.count() == 2 && stopComponent.count() == 2) {
                val startDate = LocalTime.parse("${campaign.startHour}")
                val stopDate = LocalTime.parse("${campaign.stopHour}")

                val result = now > startDate && now < stopDate

                if (result) {
                    GlobalLogger.shared.info(null, "TimeSlotFilter: ${campaign.name} CAN create notification slot date: $now - start date: $startDate - stop date: $stopDate")
                } else {
                    GlobalLogger.shared.info(null, "TimeSlotFilter: ${campaign.name} CAN NOT create notification slot date: $now - start date: $startDate - stop date: $stopDate")
                }

                return result
            }
        }

        GlobalLogger.shared.info(null, "TimeSlotFilter: can create notification no slot")
        return true
    }
}
