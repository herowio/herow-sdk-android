package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.herow.sdk.connection.cache.model.Zone

@Dao
interface ZoneDAO {

    @Insert
    fun insertZone(zone: Zone)

    @Query("SELECT * FROM Zone WHERE hash = :hash")
    fun getZoneByHash(hash: String): Zone?

    @Transaction
    @Query("SELECT * FROM Zone")
    fun getAllZones(): List<Zone>?
}