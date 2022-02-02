package io.herow.sdk.common.data

import com.google.gson.Gson

data class ZonePrediction(override var tag: String, override var pattern: LocationPattern) : IPredictable {

    fun decodeFromJson(source: String): ZonePrediction? =
        Gson().fromJson(source, ZonePrediction::class.java)
}
