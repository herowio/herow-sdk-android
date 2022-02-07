package io.herow.sdk.detection.livemoment.model.shape

import android.location.Location
import kotlin.math.PI

data class Circle(
    var radius: Double,
    var center: Location
) {
    fun area(): Double = radius * radius * PI
}