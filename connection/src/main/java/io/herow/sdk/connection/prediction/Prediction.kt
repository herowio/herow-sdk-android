package io.herow.sdk.connection.prediction

import io.herow.sdk.connection.cache.model.Poi

data class Prediction(
    val pois: ArrayList<Poi>,
    val coordinates: Coordinates
)