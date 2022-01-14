package io.herow.sdk.livemoment.model

import io.herow.sdk.livemoment.IPeriod
import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation
import java.util.*

class HerowPeriod(
    var workLocations: ArrayList<IQuadTreeLocation>,
    var homeLocations: ArrayList<IQuadTreeLocation>,
    var schoolLocations: ArrayList<IQuadTreeLocation>,
    var otherLocations: ArrayList<IQuadTreeLocation>,
    var poiLocations: ArrayList<IQuadTreeLocation>,
    var start: Date,
    var end: Date
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