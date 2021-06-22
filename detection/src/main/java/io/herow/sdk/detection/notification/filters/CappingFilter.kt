package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Capping
import io.herow.sdk.connection.cache.model.HerowCapping
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max

object CappingFilter : NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        val currentLocalDateTime = TimeHelper.getCurrentLocalDateTime()
        val oneDayInMillisSecond = 86400000.0
        val capping: Capping? = campaign.capping
        val maxCapping = capping?.maxNumberNotifications

        var resetDelay = (capping?.minTimeBetweenTwoNotifications?.toDouble()
            ?: oneDayInMillisSecond)

        resetDelay = max(resetDelay, oneDayInMillisSecond)

        var startHour: Int? = null
        var startMinutes: Int? = null
        val startFromCampaign: String? = campaign.startHour

        if (startFromCampaign != null || startFromCampaign != "") {
            val startComponents = startFromCampaign!!.split(":")

            if (startComponents.count() == 2) {
                startHour = startComponents[0].toInt()
                startMinutes = startComponents[1].toInt()
            }
        }

        GlobalLogger.shared.info(null, "Campaign is: $campaign")
        GlobalLogger.shared.info(null, "Reset delay is: $resetDelay")
        GlobalLogger.shared.info(null, "Start hour is: $startHour && start minutes is: $startMinutes")

        val firstRazDate: LocalDateTime =
            if (resetDelay > oneDayInMillisSecond) {
                val nextRazTime =
                    LocalDateTime.now().plus(resetDelay.toLong(), ChronoUnit.MILLIS)
                nextRazTime.withHour(startHour ?: 0).withMinute(startMinutes ?: 0)
            } else {
                val tomorrow = LocalDateTime.now().plus(1, ChronoUnit.DAYS)
                tomorrow.withHour(startHour ?: 0).withMinute(startMinutes ?: 0)
            }

        GlobalLogger.shared.info(null, "First raz date is: $firstRazDate")

        val herowCapping = getHerowCapping(sessionHolder, campaign, firstRazDate)
        GlobalLogger.shared.info(null, "HerowCapping is: $herowCapping")

        val count = if (currentLocalDateTime < herowCapping.razDate) {
            herowCapping.count
        } else {
            val nextRazDate = herowCapping.razDate.plus(resetDelay.toLong(), ChronoUnit.MILLIS)
            herowCapping.razDate = nextRazDate.withHour(startHour ?: 0).withMinute(startMinutes ?: 0)
            0
        }

        GlobalLogger.shared.info(null, "Count is: $count")

        herowCapping.count = count + 1
        saveHerowCapping(herowCapping, sessionHolder)

        return maxCapping?.let { count < it } ?: true
    }

    private fun getHerowCapping(
        sessionHolder: SessionHolder,
        campaign: Campaign,
        firstRazDate: LocalDateTime
    ): HerowCapping {
        return if (sessionHolder.hasHerowCappingSaved()) {
            GlobalLogger.shared.info(null, "HerowCapping exists")
            sessionHolder.getHerowCapping()
        } else {
            GlobalLogger.shared.info(null, "HerowCapping creation")
            HerowCapping(
                campaignId = campaign.id!!,
                razDate = firstRazDate,
                count = 0
            )
        }
    }
    
    private fun saveHerowCapping(herowCapping: HerowCapping, sessionHolder: SessionHolder) {
        GlobalLogger.shared.info(null, "HerowCapping exists")
        val herowCappingToString = GsonProvider.toJson(herowCapping, HerowCapping::class.java)
        GlobalLogger.shared.info(null, "HerowCapping crash?")
        sessionHolder.saveHerowCapping(herowCappingToString)
    }
}