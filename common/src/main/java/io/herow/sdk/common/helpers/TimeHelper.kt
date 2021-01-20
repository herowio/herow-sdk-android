package io.herow.sdk.common.helpers

import android.os.Build
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*

object TimeHelper {
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