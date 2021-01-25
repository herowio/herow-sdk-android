package io.herow.sdk.common.helpers

import android.os.Build
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*

object TimeHelper {
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

    /**
     * Be careful the ThreeTen library will be outdated in the future, check the repository to now
     * when and update the code in consequence
     * @see: https://github.com/JakeWharton/ThreeTenABP
     * @see: https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android
     */
    fun getCurrentTime(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.now()
            DateTimeUtils.toDate(instant).time
        } else {
            val defaultTimeZone = TimeZone.getDefault()
            val zonedDateTime = ZonedDateTime.now(ZoneId.of(defaultTimeZone.id))
            DateTimeUtils.toDate(zonedDateTime.toInstant()).time
        }
    }

    fun getUtcOffset(): Int {
        val timeZone = TimeZone.getDefault()
        return timeZone.getOffset(System.currentTimeMillis())
    }
}