package io.herow.sdk.livemoment.quadtree

import java.util.*

interface IQuadTreeLocation {
    var lat: Double
    var lng: Double
    var time: Date

    fun isNearToPoi(): Boolean
    fun setIsNearToPoi(isNear: Boolean)
}