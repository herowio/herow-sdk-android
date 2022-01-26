package io.herow.sdk.common.helpers

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.Month
import java.util.*

/**
 * ********** EXTENSIONS ***********
 */

fun Date.isHomeCompliant(): Boolean {
    val evening2000 = LocalTime.of(20, 0)
    val evening2359 = LocalTime.of(23, 59)
    val morning0000 = LocalTime.of(0, 0)
    val morning0600 = LocalTime.of(6, 0)

    val evening = evening2000 < LocalTime.now() && evening2359 > LocalTime.now()
    val morning = morning0000 < LocalTime.now() && morning0600 > LocalTime.now()

    return (evening || morning)
}

fun Date.isSchoolCompliant(): Boolean {
    val morning0840 = LocalTime.of(8, 40)
    val morning0850 = LocalTime.of(8, 50)
    val morning1130 = LocalTime.of(11, 30)
    val morning1200 = LocalTime.of(12, 0)
    val afternoon1320 = LocalTime.of(13, 20)
    val afternoon1350 = LocalTime.of(13, 50)
    val afternoon1630 = LocalTime.of(16, 30)
    val afternoon1700 = LocalTime.of(17, 0)

    val morningEntry = morning0840 < LocalTime.now() && morning0850 > LocalTime.now()
    val morningExit = morning1130 < LocalTime.now() && morning1200 > LocalTime.now()
    val afterEntry = afternoon1320 < LocalTime.now() && afternoon1350 > LocalTime.now()
    val afterExit = afternoon1630 < LocalTime.now() && afternoon1700 > LocalTime.now()

    val currentDate = TimeHelper.getCurrentLocalDateTime()
    val isNotDayOff: Boolean =
        currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY && currentDate.dayOfWeek != DayOfWeek.WEDNESDAY
    val isNotMonthOff: Boolean =
        currentDate.month != Month.JULY && currentDate.month != Month.AUGUST

    return (morningEntry || morningExit || afterEntry || afterExit) && isNotDayOff && isNotMonthOff
}

fun Date.isWorkCompliant(): Boolean {
    val work0900 = LocalTime.of(9, 0)
    val work12000 = LocalTime.of(12, 0)
    val work1400 = LocalTime.of(14, 0)
    val work1800 = LocalTime.of(18, 0)

    val morning = work0900 < LocalTime.now() && work12000 > LocalTime.now()
    val afternoon = work1400 < LocalTime.now() && work1800 > LocalTime.now()
    val currentDate = TimeHelper.getCurrentLocalDateTime()

    val isNotWeekEnd: Boolean =
        currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY

    return (afternoon || morning) && isNotWeekEnd && !isSchoolCompliant()
}