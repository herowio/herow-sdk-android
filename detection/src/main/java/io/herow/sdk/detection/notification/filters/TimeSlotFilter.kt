package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import java.time.LocalTime

object TimeSlotFilter: NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        val now = TimeHelper.getCurrentLocalTime()
        var result = true
        if (!campaign.startHour.isNullOrEmpty() && !campaign.stopHour.isNullOrEmpty()) {
            val startComponent = campaign.startHour!!.split(":")
            val stopComponent = campaign.stopHour!!.split(":")

            if (startComponent.count() == 2 && stopComponent.count() == 2) {
                val startHour = LocalTime.parse("${campaign.startHour}")
                val stopHour = LocalTime.parse("${campaign.stopHour}")

                result = now > startHour && now < stopHour

                if (result) {
                    GlobalLogger.shared.info(null, "TimeSlotFilter: ${campaign.name} CAN create notification slot date: $now - start date: $startHour - stop date: $stopHour")
                } else {
                    GlobalLogger.shared.info(null, "TimeSlotFilter: ${campaign.name} CAN NOT create notification slot date: $now - start date: $startHour - stop date: $stopHour")
                }
            }
        }

        GlobalLogger.shared.debug(null,"TimeSlotFilter will display: $result for campaign $campaign")
        return result
    }
}
