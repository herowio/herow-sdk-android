package io.herow.sdk.livemoment.prediction

import io.herow.sdk.livemoment.moment.ILiveMomentStoreListener

interface IPredictionStore : ILiveMomentStoreListener {
    fun registerListener(listener: IPredictionStoreListener)
    fun unregisterListener(listener: IPredictionStoreListener)
}