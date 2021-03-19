package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Zone

interface IZoneRepository {

    suspend fun insert(zone: Zone)
    suspend fun getAllZones(): List<Zone>?
}