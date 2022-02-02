package io.herow.sdk.connection.prediction

import android.location.Location

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

fun ArrayList<Location>.convertToCoordinates(locations: ArrayList<Location>): ArrayList<Coordinates> {
    val coordinates = arrayListOf<Coordinates>()

    for (location in locations) {
        coordinates.add(Coordinates(location.latitude, location.longitude))
    }

    return coordinates
}