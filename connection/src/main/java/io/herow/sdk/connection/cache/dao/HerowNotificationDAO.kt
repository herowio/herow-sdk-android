package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.herow.sdk.connection.cache.model.HerowNotification

@Dao
interface HerowNotificationDAO {

    @Insert
    fun insert(herowNotification: HerowNotification)

    @Query("SELECT herowNotificationID, title, description FROM HerowNotification LIMIT 50")
    fun getFiftyFirstNotifications(): List<HerowNotification>?
}