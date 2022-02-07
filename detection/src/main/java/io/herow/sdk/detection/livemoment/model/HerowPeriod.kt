package io.herow.sdk.detection.livemoment.model

import androidx.room.Entity
import io.herow.sdk.detection.livemoment.IPeriod
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeLocation
import java.time.LocalDate

@Entity
class HerowPeriod(
    var otherLocations: ArrayList<HerowQuadTreeLocation>,
    var poiLocations: ArrayList<HerowQuadTreeLocation>,
    override var end: LocalDate,
    override var start: LocalDate,
    override var homeLocations: ArrayList<HerowQuadTreeLocation>,
    override var workLocations: ArrayList<HerowQuadTreeLocation>,
    override var schoolLocations: ArrayList<HerowQuadTreeLocation>,
    override var shoppingLocations: ArrayList<HerowQuadTreeLocation>,
    override var othersLocations: ArrayList<HerowQuadTreeLocation>,
    override var poisLocations: ArrayList<HerowQuadTreeLocation>
) :
    IPeriod {

    override fun getAllLocations(): ArrayList<HerowQuadTreeLocation> {
        val listOfAllLocations = ArrayList<HerowQuadTreeLocation>()
        listOfAllLocations.addAll(workLocations)
        listOfAllLocations.addAll(homeLocations)
        listOfAllLocations.addAll(schoolLocations)
        listOfAllLocations.addAll(otherLocations)
        listOfAllLocations.addAll(poiLocations)

        return listOfAllLocations.sortedBy { start > end } as ArrayList<HerowQuadTreeLocation>
    }
}