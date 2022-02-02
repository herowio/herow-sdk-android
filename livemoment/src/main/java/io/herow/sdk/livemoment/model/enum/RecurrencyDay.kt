package io.herow.sdk.livemoment.model.enum

class RecurrencyDay(private val day: Day, private val slot: RecurrencySlot) {

    fun rawValue(): String = when (day) {
        Day.MONDAY -> "monday ${slot.name}"
        Day.TUESDAY -> "tuesday ${slot.name}"
        Day.WEDNESDAY -> "wednesday ${slot.name}"
        Day.THURSDAY -> "thursday ${slot.name}"
        Day.FRIDAY -> "friday ${slot.name}"
        Day.SATURDAY -> "saturday ${slot.name}"
        Day.SUNDAY -> "sunday ${slot.name}"
        else -> "RecurrencyDay slot"
    }
}