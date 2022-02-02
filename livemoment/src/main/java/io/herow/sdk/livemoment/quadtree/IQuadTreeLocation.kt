package io.herow.sdk.livemoment.quadtree

import java.time.LocalDate

interface IQuadTreeLocation {
    var lat: Double
    var lng: Double
    var time: LocalDate

    fun isNearToPoi(): Boolean
    fun setIsNearToPoi(isNear: Boolean)
}