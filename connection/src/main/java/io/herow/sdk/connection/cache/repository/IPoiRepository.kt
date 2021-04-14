package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Poi

interface IPoiRepository {

    fun insert(poi: Poi)
    fun getAllPois(): List<Poi>?
}