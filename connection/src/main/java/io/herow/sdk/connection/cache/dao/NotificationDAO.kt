package io.herow.sdk.connection.cache.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import io.herow.sdk.connection.cache.model.Notification

@Dao
interface NotificationDAO {

    @Insert
    fun insertNotification(vararg notification: Notification)
}