package io.herow.sdk.common.helpers

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

object TimeHelper {
    const val TWO_SECONDS_MS = 2 * 1000L
    const val FIVE_SECONDS_MS = 5 * 1000L
    const val TEN_SECONDS_MS = 10 * 1000L
    const val TWENTY_SECONDS_MS = 20 * 1000L
    const val THIRTY_SECONDS_MS = 30 * 1000L
    const val ONE_MINUTE_MS = 60 * 1000L
    const val THREE_MINUTE_MS = 3 * 60 * 1000L
    const val FIVE_MINUTES_MS = 5 * 60 * 1000L
    const val TEN_MINUTES_MS = 10 * 60 * 1000L
    const val FIFTEEN_MINUTES_MS = 15 * 60 * 1000L
    const val ONE_HOUR_MS = 60 * 60 * 1000L
    const val TWO_HOUR_MS = 2 * 60 * 60 * 1000L
    const val TWENTY_FOUR_HOUR_MS = 24 * 60 * 60 * 1000L
    var testing = false

    fun getCurrentTime(): Long = System.currentTimeMillis()

    fun getCurrentLocalTime(): LocalTime = LocalTime.now()

    fun convertDateToMilliSeconds(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!!
}