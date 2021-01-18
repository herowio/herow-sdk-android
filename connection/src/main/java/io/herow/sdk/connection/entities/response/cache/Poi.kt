package io.herow.sdk.connection.entities.response.cache

data class Poi(val id: String,
               val lat: Double,
               val lng: Double,
               val tags: List<String>)