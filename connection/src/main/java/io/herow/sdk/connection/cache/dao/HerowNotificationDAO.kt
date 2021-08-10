package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.herow.sdk.connection.cache.model.HerowNotification

@Dao
interface HerowNotificationDAO {

    @Insert
    fun insert(herowNotification: HerowNotification)

    @Query("SELECT herowNotificationID, owner, title, description FROM HerowNotification WHERE owner = :owner LIMIT 50")
    fun getFiftyFirstNotifications(owner: String): List<HerowNotification>?
}