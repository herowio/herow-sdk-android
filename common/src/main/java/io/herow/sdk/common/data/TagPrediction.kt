package io.herow.sdk.common.data

import com.google.gson.Gson

data class TagPrediction(
    override var tag: String,
    override var pattern: LocationPattern
) : IPredictable {

    fun decodeFromJson(source: String): TagPrediction? =
        Gson().fromJson(source, TagPrediction::class.java)
}

typealias LocationPattern = HashMap<String, Double>