package io.herow.sdk.detection.livemoment.prediction

import io.herow.sdk.detection.livemoment.moment.ILiveMomentStoreListener

interface IPredictionStore : ILiveMomentStoreListener {
    fun registerListener(listener: IPredictionStoreListener)
    fun unregisterListener(listener: IPredictionStoreListener)
}