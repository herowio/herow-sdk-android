package io.herow.sdk.detection.livemoment.model.dao

import androidx.room.Insert
import androidx.room.Query
import io.herow.sdk.detection.livemoment.model.HerowPeriod

interface IPeriodDAO {

    @Insert
    fun insertPeriod(period: HerowPeriod)

    @Query("SELECT * FROM HerowPeriod")
    fun getAllPeriods(): ArrayList<HerowPeriod>?
}