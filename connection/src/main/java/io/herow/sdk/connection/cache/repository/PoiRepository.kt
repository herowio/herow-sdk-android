package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.model.Poi

class PoiRepository(private val poiDAO: PoiDAO) : IPoiRepository {

    override fun insert(poi: Poi) {
        poiDAO.insertPOI(poi)
    }

    override fun getAllPois(): List<Poi>? = poiDAO.getAllPois()
}