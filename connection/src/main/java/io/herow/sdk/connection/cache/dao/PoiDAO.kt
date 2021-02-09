package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import io.herow.sdk.connection.cache.model.Poi

@Dao
interface PoiDAO {

    @Insert
    fun insertPOI(vararg poi: Poi)
}