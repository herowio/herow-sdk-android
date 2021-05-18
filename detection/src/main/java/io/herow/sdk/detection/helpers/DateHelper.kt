package io.herow.sdk.detection.helpers

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DateHelper {

    fun convertStringToTimeStamp(stringDate: String): Long {
        return LocalDateTime.parse(stringDate, DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond(
            ZoneOffset.UTC
        )
    }

    fun getCurrentWeekDay(): String = LocalDate.now().dayOfWeek.name.uppercase()
}