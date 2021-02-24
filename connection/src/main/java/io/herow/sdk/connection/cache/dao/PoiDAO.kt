package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.herow.sdk.connection.cache.model.Poi

@Dao
interface PoiDAO {

    @Insert
    fun insertPOI(poi: Poi)

    @Query("SELECT * FROM POI")
    fun getAllPois(): List<Poi>?
}