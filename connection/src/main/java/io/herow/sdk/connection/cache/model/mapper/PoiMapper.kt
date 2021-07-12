package io.herow.sdk.connection.cache.model.mapper

data class PoiMapper(
    var id: String = "",
    var distance: Double = 0.0,
    var tags: List<String>? = listOf()
)