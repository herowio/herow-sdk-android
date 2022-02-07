package io.herow.sdk.detection.livemoment

import io.herow.sdk.common.helpers.isHomeCompliant
import io.herow.sdk.common.helpers.isSchoolCompliant
import io.herow.sdk.common.helpers.isWorkCompliant
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeLocation
import java.time.LocalDate

interface IPeriod {
    var end: LocalDate
    var start: LocalDate

    var homeLocations: ArrayList<HerowQuadTreeLocation>
    var workLocations: ArrayList<HerowQuadTreeLocation>
    var schoolLocations: ArrayList<HerowQuadTreeLocation>
    var shoppingLocations: ArrayList<HerowQuadTreeLocation>
    var othersLocations: ArrayList<HerowQuadTreeLocation>
    var poisLocations: ArrayList<HerowQuadTreeLocation>

    fun getAllLocations(): ArrayList<HerowQuadTreeLocation>
    fun containsLocation(location: HerowQuadTreeLocation): Boolean =
        start < location.time && end > location.time

    fun addLocation(location: HerowQuadTreeLocation) {
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