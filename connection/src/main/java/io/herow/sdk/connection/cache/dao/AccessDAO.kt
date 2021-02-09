package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import io.herow.sdk.connection.cache.model.Access

@Dao
interface AccessDAO {

    @Insert
    fun insertAccess(vararg access: Access)
}