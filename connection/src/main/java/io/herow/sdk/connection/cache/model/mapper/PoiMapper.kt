package io.herow.sdk.connection.cache.model.mapper

data class PoiMapper(
    var id: String = "",
    var distance: Float = 0f,
    var tags: List<String>? = listOf()
)