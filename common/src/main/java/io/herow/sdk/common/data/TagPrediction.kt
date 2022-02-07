package io.herow.sdk.common.data

data class TagPrediction(override var tag: String, override var pattern: LocationPattern) : IPredictable
typealias LocationPattern = HashMap<String, Double>