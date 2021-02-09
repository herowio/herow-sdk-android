package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import io.herow.sdk.connection.cache.model.Capping

@Dao
interface CappingDAO {

    @Insert
    fun insertCapping(vararg capping: Capping)
}