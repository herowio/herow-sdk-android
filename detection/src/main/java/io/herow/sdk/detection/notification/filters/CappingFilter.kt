package io.herow.sdk.detection.notification.filters

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Capping
import io.herow.sdk.connection.cache.model.HerowCapping
import io.herow.sdk.connection.cache.model.mapper.HerowCappingMapper
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max

object CappingFilter : NotificationFilter {

    override fun createNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        val currentLocalDateTime = TimeHelper.getCurrentLocalDateTime()
        val oneDayInMillisSecond = 86400000
        val capping: Capping? = campaign.capping
        val maxCapping = capping?.maxNumberNotifications

        var resetDelay = (capping?.minTimeBetweenTwoNotifications?.toDouble()
            ?: oneDayInMillisSecond.toDouble()).div(1000)

        resetDelay = max(resetDelay, oneDayInMillisSecond.toDouble())

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

        val firstRazDate: LocalDateTime =
            if (resetDelay > (oneDayInMillisSecond / 1000).toDouble()) {
                val nextRazTime =
                    LocalDateTime.now().plus(resetDelay.toLong(), ChronoUnit.MILLIS)
                nextRazTime.withHour(startHour ?: 0).withMinute(startMinutes ?: 0)
            } else {
                val tomorrow = LocalDateTime.now().plus(1, ChronoUnit.DAYS)
                tomorrow.withHour(startHour ?: 0).withMinute(startMinutes ?: 0)
            }

        val herowCapping = getHerowCapping(sessionHolder, campaign, firstRazDate)

        val count: Int

        if (currentLocalDateTime < herowCapping.razDate) {
            count = herowCapping.count
        } else {
            count = 0
            val nextRazDate =
                herowCapping.razDate.plus(resetDelay.toLong(), ChronoUnit.MILLIS)
            herowCapping.razDate =
                nextRazDate.withHour(startHour ?: 0).withMinute(startMinutes ?: 0)
        }

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
            val herowCappingMapper = sessionHolder.getHerowCapping()
            HerowCapping().convertMapperToCapping(herowCappingMapper)
        } else {
            HerowCapping(
                campaignId = campaign.id!!,
                razDate = firstRazDate,
                count = 0
            )
        }
    }
    
    private fun saveHerowCapping(herowCapping: HerowCapping, sessionHolder: SessionHolder) {
        val herowCappingMapper = HerowCappingMapper().convertCappingToMapper(herowCapping)

        val herowCappingToString =
            GsonProvider.toJson(herowCappingMapper, HerowCappingMapper::class.java)
        sessionHolder.saveHerowCapping(herowCappingToString)
    }
}