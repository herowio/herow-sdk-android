package io.herow.sdk.common

import java.util.*

object TimeHelper {
    fun getUtcOffset(): Int {
        val timeZone = TimeZone.getDefault()
        return timeZone.getOffset(System.currentTimeMillis())
    }
}