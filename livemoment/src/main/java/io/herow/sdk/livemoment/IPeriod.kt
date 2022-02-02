package io.herow.sdk.livemoment

import io.herow.sdk.common.helpers.isHomeCompliant
import io.herow.sdk.common.helpers.isSchoolCompliant
import io.herow.sdk.common.helpers.isWorkCompliant
import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation
import java.time.LocalDate
import kotlin.collections.ArrayList

interface IPeriod {
    var end: LocalDate
    var start: LocalDate

    var homeLocations: ArrayList<IQuadTreeLocation>
    var workLocations: ArrayList<IQuadTreeLocation>
    var schoolLocations: ArrayList<IQuadTreeLocation>
    var shoppingLocations: ArrayList<IQuadTreeLocation>
    var othersLocations: ArrayList<IQuadTreeLocation>
    var poisLocations: ArrayList<IQuadTreeLocation>

    fun getAllLocations(): ArrayList<IQuadTreeLocation>
    fun containsLocation(location: IQuadTreeLocation): Boolean =
        start < location.time && end > location.time

    fun addLocation(location: IQuadTreeLocation) {
        if (containsLocation(location)) {
            if (location.time.isHomeCompliant()) {
                homeLocations.add(location)
            } else if (location.time.isWorkCompliant()) {
                workLocations.add(location)
            } else if (location.time.isSchoolCompliant()) {
                schoolLocations.add(location)
            } else {
                othersLocations.add(location)
            }

            if (location.isNearToPoi()) {
                poisLocations.add(location)
            }
        }
    }
}