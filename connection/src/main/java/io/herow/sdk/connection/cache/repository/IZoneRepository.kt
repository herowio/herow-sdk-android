package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.Zone

interface IZoneRepository {
    fun insert(zone: Zone)
    fun getZoneByHash(hash: String): Zone?
    fun getAllZones(): List<Zone>?
}