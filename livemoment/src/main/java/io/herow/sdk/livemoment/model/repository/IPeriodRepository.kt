package io.herow.sdk.livemoment.model.repository

import io.herow.sdk.livemoment.model.HerowPeriod

interface IPeriodRepository {
    fun insertPeriod(period: HerowPeriod)
    fun getAllPeriods(): ArrayList<HerowPeriod>?
}