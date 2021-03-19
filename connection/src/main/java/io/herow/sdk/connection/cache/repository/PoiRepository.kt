package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.model.Poi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PoiRepository constructor(
    private val poiDAO: PoiDAO
) :
    IPoiRepository {

    override suspend fun insert(poi: Poi) = poiDAO.insertPOI(poi)
    override suspend fun getAllPois(): List<Poi>? = poiDAO.getAllPois()
}