package io.herow.sdk.livemoment.quadtree

import java.time.LocalDate

class HerowQuadTreeLocation(override var lat: Double, override var lng: Double, override var time: LocalDate) :
    IQuadTreeLocation {
    var nearToPoi: Boolean = false

    override fun isNearToPoi(): Boolean = nearToPoi

    override fun setIsNearToPoi(isNear: Boolean) {
        nearToPoi = isNear
    }
}