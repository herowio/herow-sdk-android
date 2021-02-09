package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import io.herow.sdk.connection.cache.model.Trigger

@Dao
interface TriggerDAO {

    @Insert
    fun insertTrigger(vararg trigger: Trigger)
}