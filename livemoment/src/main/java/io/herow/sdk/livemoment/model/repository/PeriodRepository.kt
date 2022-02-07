package io.herow.sdk.livemoment.model.repository

import io.herow.sdk.livemoment.model.HerowPeriod
import io.herow.sdk.livemoment.model.dao.IPeriodDAO

class PeriodRepository(private val periodDAO: IPeriodDAO) : IPeriodRepository {

    override fun insertPeriod(period: HerowPeriod) = periodDAO.insertPeriod(period)
    override fun getAllPeriods(): ArrayList<HerowPeriod>? = periodDAO.getAllPeriods()
}