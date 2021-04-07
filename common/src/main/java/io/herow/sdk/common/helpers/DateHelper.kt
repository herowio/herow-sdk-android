package io.herow.sdk.common.helpers

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

object DateHelper {

    fun convertDateToMilliSeconds(dateTime: LocalDateTime, context: Context): Long {
        AndroidThreeTen.init(context)
        val zoneDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault())

        return zoneDateTime.toInstant().toEpochMilli()
    }
}