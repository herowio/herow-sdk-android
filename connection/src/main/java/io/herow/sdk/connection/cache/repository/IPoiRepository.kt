package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Poi

interface IPoiRepository {

    suspend fun insert(poi: Poi)
    suspend fun getAllPois(): List<Poi>?
}