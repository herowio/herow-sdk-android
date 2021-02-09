package io.herow.sdk.connection.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Notification"
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val notificationID: Int,
    val notificationCampaignID: Int,
    val title: String,
    val description: String
)
