package io.herow.sdk.livemoment.prediction

import io.herow.sdk.connection.prediction.Prediction

interface IPredictionStoreListener {
    fun didPredict(predictions: ArrayList<Prediction>)
}