package io.herow.sdk.detection.livemoment.prediction

import io.herow.sdk.connection.prediction.Prediction

interface IPredictionStoreListener {
    fun didPredict(predictions: ArrayList<Prediction>)
}