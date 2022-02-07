package io.herow.sdk.livemoment.model

import androidx.room.Entity
import io.herow.sdk.livemoment.IPeriod
import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation
import java.time.LocalDate
import kotlin.collections.ArrayList

@Entity
class HerowPeriod(
    var otherLocations: ArrayList<IQuadTreeLocation>,
    var poiLocations: ArrayList<IQuadTreeLocation>,
    override var end: LocalDate,
    override var start: LocalDate,
    override var homeLocations: ArrayList<IQuadTreeLocation>,
    override var workLocations: ArrayList<IQuadTreeLocation>,
    override var schoolLocations: ArrayList<IQuadTreeLocation>,
    override var shoppingLocations: ArrayList<IQuadTreeLocation>,
    override var othersLocations: ArrayList<IQuadTreeLocation>,
    override var poisLocations: ArrayList<IQuadTreeLocation>
) :
    IPeriod {

    override fun getAllLocations(): ArrayList<IQuadTreeLocation> {
        val listOfAllLocations = ArrayList<IQuadTreeLocation>()
        listOfAllLocations.addAll(workLocations)
        listOfAllLocations.addAll(homeLocations)
        listOfAllLocations.addAll(schoolLocations)
        listOfAllLocations.addAll(otherLocations)
        listOfAllLocations.addAll(poiLocations)

        return listOfAllLocations.sortedBy { start > end } as ArrayList<IQuadTreeLocation>
    }
}