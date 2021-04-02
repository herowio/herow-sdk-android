package io.herow.sdk.connection.cache.model.mapper

data class ZoneMapper(
    var lng: Double? = 0.0,
    var lat: Double? = 0.0,
    var place_id: String = "",
    var distance: Double = 0.0,
    var radius: Double? = 0.0
)