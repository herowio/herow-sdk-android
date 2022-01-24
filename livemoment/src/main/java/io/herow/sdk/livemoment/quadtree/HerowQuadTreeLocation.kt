package io.herow.sdk.livemoment.quadtree

import java.util.*

class HerowQuadTreeLocation(override var lat: Double, override var lng: Double, override var time: Date): IQuadTreeLocation {
    var nearToPoi: Boolean = false

    override fun isNearToPoi(): Boolean = nearToPoi

    override fun setIsNearToPoi(isNear: Boolean) {
        nearToPoi = isNear
    }
}