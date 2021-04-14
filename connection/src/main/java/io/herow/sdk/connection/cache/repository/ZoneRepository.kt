package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.Zone

class ZoneRepository constructor(
    private val zoneDAO: ZoneDAO
) :
    IZoneRepository {

    override fun insert(zone: Zone) = zoneDAO.insertZone(zone)
    override fun getAllZones(): List<Zone>? = zoneDAO.getAllZones()
}