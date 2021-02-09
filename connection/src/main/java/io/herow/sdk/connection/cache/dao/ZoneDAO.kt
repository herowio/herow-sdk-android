package io.herow.sdk.connection.cache.dao

import androidx.room.*
import io.herow.sdk.connection.cache.model.Zone

@Dao
interface ZoneDAO {

    @Insert
    fun insertZone(vararg zone: Zone)

    @Transaction
    @Query("SELECT * FROM Zone")
    fun getAllZone(): List<Zone>
}