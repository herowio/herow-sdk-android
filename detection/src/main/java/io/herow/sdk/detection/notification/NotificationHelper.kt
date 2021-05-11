package io.herow.sdk.detection.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.detection.notification.filters.CappingFilter
import io.herow.sdk.detection.notification.filters.DayRecurrencyFilter
import io.herow.sdk.detection.notification.filters.TimeSlotFilter
import io.herow.sdk.detection.notification.filters.ValidityFilter
import io.herow.sdk.detection.notification.model.DynamicKeys
import io.herow.sdk.detection.notification.model.DynamicResult
import java.util.regex.Pattern

object NotificationHelper {

    const val CHANNEL_ID: String = "Campaign channel ID"

    fun setUpNotificationChannel(context: Context): NotificationManager {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification for campaign",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationManager
    }

    fun hashCode(string: String?): Int {
        val prime = 31
        return if (string != null) string.hashCode() * prime else 0 // PRIME = 31 or another prime number.
    }

    fun computeDynamicContent(
        text: String,
        zone: Zone,
        sessionHolder: SessionHolder
    ): String {
        val dynamicResult = dynamicValues(text)
        var result: String = dynamicResult.newText
        val defaultValue: HashMap<String, String> = dynamicResult.defaultValue

        GlobalLogger.shared.debug(null, "DynamicResult is: $dynamicResult")

        for (key in DynamicKeys.values()) {
            val valueToDisplay: String? = when (key.value) {
                DynamicKeys.NAME.value -> if (zone.access?.name?.isNotEmpty() == true) {
                    zone.access?.name
                } else {
                    defaultValue[key.value]
                }

                DynamicKeys.RADIUS.value -> if (zone.radius != null) {
                    zone.radius.toString()
                } else {
                    defaultValue[key.value]
                }

                DynamicKeys.ADDRESS.value -> if (zone.access?.address?.isNotEmpty() == true) {
                    zone.access?.address
                } else {
                    defaultValue[key.value]
                }

                DynamicKeys.CUSTOM_ID.value -> if (sessionHolder.getCustomID().isNotEmpty()) {
                    sessionHolder.getCustomID()
                } else {
                    defaultValue[key.value]
                }
                else -> "Key not recognized"
            }

            GlobalLogger.shared.debug(null, "ValueToDisplay is: $valueToDisplay")

            result = result.replace(key.value, valueToDisplay ?: "")
        }

        GlobalLogger.shared.debug(null, "Result sent is: $result")

        return result
    }

    private fun dynamicValues(sentenceToAnalyze: String): DynamicResult {
        GlobalLogger.shared.debug(null, "Regex is: $sentenceToAnalyze")
        var couple = HashMap<String, String>()

        val newText = sentenceToAnalyze
            .replace("{{", "")
            .replace("}}", "")

        GlobalLogger.shared.debug(null, "Result is: $newText")

        val components = newText.split("|")
        GlobalLogger.shared.debug(null, "Components to return: $components")

        val sentenceNotExtracted = sentenceToAnalyze
            .replace("default('", "")
            .replace("')", "")

        if (components.size > 1) {
            couple = extractDefaultValue(sentenceNotExtracted)
        }

        return DynamicResult(removeRegex(newText), couple)
    }

    private fun removeRegex(sentence: String): String {
        val regex = "\\|([a-z A-Z0-9]*)\\('[a-z A-Z0-9-]*'\\)"
        val pattern = Pattern.compile(regex, Pattern.MULTILINE)
        val matcher = pattern.matcher(sentence)

        return matcher.replaceAll("")
    }

    private fun extractDefaultValue(sentence: String): HashMap<String, String> {
        val couple = HashMap<String, String>()
        var searchingPosition = 0

        while (searchingPosition >= 0) {
            var insertingKey: String
            var insertingValue: String

            searchingPosition = sentence.indexOf("{{", searchingPosition)

            if (searchingPosition > -1) {
                val separator = sentence.indexOf("|", searchingPosition)
                insertingKey = sentence.substring(searchingPosition + 2, separator)

                GlobalLogger.shared.debug(null, "InsertingKey is: $insertingKey")

                val endPosition = sentence.indexOf("}}", searchingPosition)
                insertingValue = sentence.substring(separator + 1, endPosition)

                GlobalLogger.shared.debug(null, "InsertingValue is: $insertingValue")
                couple[insertingKey] = insertingValue
                searchingPosition = endPosition + 2
            }
        }

        GlobalLogger.shared.debug(null, "Couple is: $couple")

        return couple
    }

    fun canCreateNotification(campaign: Campaign, sessionHolder: SessionHolder): Boolean {
        if (!TimeSlotFilter.createNotification(
                campaign,
                sessionHolder
            ) || !(DayRecurrencyFilter.createNotification(
                campaign, sessionHolder
            )) || !(ValidityFilter.createNotification(
                campaign,
                sessionHolder
            )) || !(CappingFilter.createNotification(campaign, sessionHolder))
        ) {
            return false
        }

        GlobalLogger.shared.warning(null, "Can create notification")
        return true
    }
}

