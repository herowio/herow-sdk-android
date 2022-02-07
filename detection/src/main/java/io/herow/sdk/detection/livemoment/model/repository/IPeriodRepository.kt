package io.herow.sdk.detection.livemoment.model.repository

import io.herow.sdk.detection.livemoment.model.HerowPeriod

interface IPeriodRepository {
    fun insertPeriod(period: HerowPeriod)
    fun getAllPeriods(): ArrayList<HerowPeriod>?
}