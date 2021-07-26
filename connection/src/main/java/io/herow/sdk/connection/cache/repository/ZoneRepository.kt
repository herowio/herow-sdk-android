package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.Zone

class ZoneRepository(
    private val zoneDAO: ZoneDAO
) :
    IZoneRepository {

    override fun insert(zone: Zone) = zoneDAO.insertZone(zone)
    override fun getZoneByHash(hash: String): Zone? = zoneDAO.getZoneByHash(hash)
    override fun getAllZones(): List<Zone>? = zoneDAO.getAllZones()

}