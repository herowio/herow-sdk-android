package io.herow.sdk.livemoment.model

import io.herow.sdk.common.data.LocationPattern
import io.herow.sdk.common.data.TagPrediction
import io.herow.sdk.common.helpers.filtered
import io.herow.sdk.livemoment.model.enum.Day
import io.herow.sdk.livemoment.model.enum.RecurrencyDay
import io.herow.sdk.livemoment.model.enum.RecurrencySlot
import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class TagObject(
    val tag: String,
    val locations: ArrayList<IQuadTreeLocation>,
    var new: Boolean = true,
    var recurrencies: HashMap<RecurrencyDay, Int> = hashMapOf()
) {

    fun addLocations(newLocations: ArrayList<IQuadTreeLocation>) {
        for (loc in newLocations) {
            addLocation(loc)
        }
    }

    private fun addLocation(location: IQuadTreeLocation) {
        locations.add(location)
        computeRecurrency(location)
    }

    fun toTagPrediction(): TagPrediction = TagPrediction(
        tag, getLocationPattern()
    )

    private fun getLocationPattern(): LocationPattern {
        val count: Double = locations.count().toDouble()
        val pattern = LocationPattern()

        for (recurrency in recurrencies) {
            val decimalFormat = DecimalFormat("#.##")
            decimalFormat.roundingMode = RoundingMode.CEILING

            pattern[recurrency.key.rawValue()] = (decimalFormat.format(recurrency.value.div(count))).toDouble()
        }

        return pattern.filtered(pattern)
    }

    private fun computeRecurrency(location: IQuadTreeLocation) {
        val day = location.time.reccurencyDay(location.time, getSlot())
        var value: Int = recurrencies[day] ?: 0
        value += 1

        recurrencies[day] = value
    }

    private fun getSlot(): RecurrencySlot {
        val currentHour = LocalDateTime.now().hour

        return if (currentHour < 6) {
            RecurrencySlot.NIGHT
        } else if (currentHour < 10) {
            RecurrencySlot.EARLY_MORNING
        } else if (currentHour < 12) {
            RecurrencySlot.LATE_MORNING
        } else if (currentHour < 14) {
            RecurrencySlot.LUNCH_TIME
        } else if (currentHour < 16) {
            RecurrencySlot.EARLY_AFTERNOON
        } else if (currentHour < 18) {
            RecurrencySlot.LATE_AFTERNOON
        } else if (currentHour < 22) {
            RecurrencySlot.EVENING
        } else {
            RecurrencySlot.NIGHT
        }
    }
}

fun LocalDate.reccurencyDay(time: LocalDate, slot: RecurrencySlot): RecurrencyDay =
    when (time.dayOfWeek) {
        DayOfWeek.MONDAY -> RecurrencyDay(Day.MONDAY, slot)
        DayOfWeek.TUESDAY -> RecurrencyDay(Day.TUESDAY, slot)
        DayOfWeek.WEDNESDAY -> RecurrencyDay(Day.WEDNESDAY, slot)
        DayOfWeek.THURSDAY -> RecurrencyDay(Day.THURSDAY, slot)
        DayOfWeek.FRIDAY -> RecurrencyDay(Day.FRIDAY, slot)
        DayOfWeek.SATURDAY -> RecurrencyDay(Day.SATURDAY, slot)
        DayOfWeek.SUNDAY -> RecurrencyDay(Day.SUNDAY, slot)
        else -> RecurrencyDay(Day.MONDAY, RecurrencySlot.UNKNOWN)
    }