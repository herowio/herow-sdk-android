package io.herow.sdk.livemoment

import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation

interface IPeriod {
    fun getAllLocations(): ArrayList<IQuadTreeLocation>
}