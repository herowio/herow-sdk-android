package io.herow.sdk.detection.helpers

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object DateHelper {

    private val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    fun convertStringToTimeStamp(stringDate: String): Long {
        return LocalDateTime.parse(stringDate, DateTimeFormatter.RFC_1123_DATE_TIME)
            .toEpochSecond(org.threeten.bp.ZoneOffset.UTC)
    }
}