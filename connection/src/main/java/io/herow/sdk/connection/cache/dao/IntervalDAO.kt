package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import io.herow.sdk.connection.cache.model.Interval

@Dao
interface IntervalDAO {

    @Insert
    fun insertInterval(vararg interval: Interval)
}